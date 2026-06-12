# 테스트 전략 검증 보고서

**일시**: 2026-06-12
**검증 대상**: docs/design/phase4.md — 조립 및 통합 시나리오 검증
**결과**: 미흡 4건 (CRITICAL: 1, WARNING: 3)

---

## 발견된 문제

### [CRITICAL] 설계 문서의 output() 픽스처가 항상 빈 문자열을 반환하는 버그 내포

- **대상 기능**: `SampleFlowIntegrationTest` 픽스처 — `output()` 메서드
- **문제**: `phase4.md` 3절 픽스처 코드 스니펫에서 `output()`이 `outContent.reset()`을 호출한
  직후 `outContent.toString()`을 반환하도록 정의되어 있다.
  `ByteArrayOutputStream.reset()`은 내부 버퍼를 비우므로, 이 구현대로라면 `output()`은
  항상 빈 문자열을 반환한다. 모든 `assertTrue(output().contains(...))` 검증이 실패한다.
  실제 구현 파일(`SampleFlowIntegrationTest.java`)은 `output()`에서 `reset()`을 제거하고
  테스트 메서드 내부에서 직접 `outContent.reset()`을 호출함으로써 이 버그를 올바르게
  우회했다. 그러나 설계 문서가 잘못된 API 사용 예시를 제시한 상태이므로, 이후 동일 문서를
  참고해 테스트를 작성하는 경우 동일 버그가 재현될 위험이 있다.
- **권장 조치**: `phase4.md` 3절 픽스처의 `output()` 코드 스니펫을 다음과 같이 수정한다.
  ```java
  // 수정 전 (설계 문서 현재 상태 — 버그)
  private String output() {
      outContent.reset();   // 이전 출력 초기화 후 재캡처가 필요한 경우 사용
      return outContent.toString(StandardCharsets.UTF_8);
  }

  // 수정 후
  private String output() {
      return outContent.toString(StandardCharsets.UTF_8);
  }
  // 출력 초기화가 필요한 시점에는 테스트 메서드 내에서 outContent.reset() 직접 호출
  ```

---

### [WARNING] 등록_수정_단건조회 시나리오에서 outContent.reset() 누락으로 출력 혼재

- **대상 기능**: `SampleFlowIntegrationTest.등록_수정_단건조회_수정된_이름_반영()`
- **문제**: 해당 테스트는 `register()` 후 `update()`, 그 후 `outContent.reset()` 없이
  바로 `findById()`를 호출한다. `output()`이 호출되면 `update()` 실행 중 출력된 프롬프트
  및 성공 메시지("[성공] 시료가 수정되었습니다.")가 `findById()` 출력에 누적된 상태로 반환된다.
  `phase4.md` 설계 주석("호출 전후 outContent.reset()으로 출력 버퍼를 초기화해 이전 출력과
  혼재되지 않도록 한다")의 의도에 부합하지 않는다.
  현재 검증 조건(`contains("BetaChip")`)은 통과하지만, 향후 `assertFalse(out.contains("[성공]"))`
  등 부정 조건을 추가하거나 `findById()` 전용 출력만 단독 검증하는 요구가 생기면 오탐이 발생한다.
- **권장 수정**:
  ```java
  @Test
  void 등록_수정_단건조회_수정된_이름_반영() {
      controller("AlphaChip\n30\n0.95\n").register();
      controller("1\n1\nBetaChip\n").update();
      outContent.reset();          // update() 출력 소거 후 findById() 출력만 검증
      controller("1\n").findById();
      assertTrue(output().contains("BetaChip"));
  }
  ```

---

### [WARNING] Main 루프의 Scanner EOF 및 닫힘 예외에 대한 테스트 계획 부재

- **대상 기능**: `Main.main()` — while 루프의 `scanner.nextLine()` 호출
- **문제**: `Main.java`는 `NumberFormatException`만 처리하고 있다. 다음 두 가지 상황에서
  비처리 예외가 발생하여 스택 트레이스와 함께 비정상 종료된다.
  - 표준 입력이 닫히거나 파이프 입력이 종료된 경우: `NoSuchElementException`
  - `Scanner`가 이미 닫힌 상태에서 `nextLine()` 호출 시: `IllegalStateException`
  CI/CD 환경이나 자동화 테스트에서 stdin이 조기 종료되는 경우 재현 가능하다.
  `phase4.md` 및 `PLAN.md` 어디에도 이 예외 흐름에 대한 테스트 케이스가 명시되지 않았다.
- **권장 조치**: Phase 5 진입 전 아래 중 하나를 선택한다.
  - `Main.java` while 루프에 `NoSuchElementException` catch 블록을 추가하고 테스트로 검증
  - 설계 문서에 "stdin 종료 시 동작은 미정의(PoC 범위 외)"임을 명시하여 의도적 제외 처리
  ```java
  // Main.java 보강 예시
  } catch (NoSuchElementException e) {
      // EOF 또는 입력 스트림 종료 — 루프 정상 탈출
      break;
  }
  ```

---

### [WARNING] Main 루프 전체 흐름에 대한 통합 테스트 부재

- **대상 기능**: `Main.main()` — `view.printMenu()` → 입력 파싱 → `router.route()` → 종료 판단 루프
- **문제**: `SampleFlowIntegrationTest`는 `Router` 또는 `SampleController` 메서드를 직접
  호출하는 방식으로 구성되어 있다. `Main.main()` 루프 자체를 통해 다음 흐름이 올바르게
  동작하는지는 검증되지 않는다.
  - 숫자 아닌 입력 수신 → "메뉴는 숫자로 입력하세요." 출력 → 루프 재시도
  - 유효 메뉴 번호 입력 → 해당 기능 실행 후 루프 재시작
  - `0` 입력 → 루프 종료
  `RouterTest`와 `SampleControllerTest`가 각 단위를 검증하더라도, 이들을 조합하는
  Main 루프의 통합 동작은 별도로 검증되어야 한다.
- **권장 테스트**: `SampleFlowIntegrationTest` 또는 별도 `MainIntegrationTest`에 추가한다.
  ```java
  @Test
  void Main_숫자아닌_입력_후_오류_메시지_출력_후_재입력_종료() {
      InputStream fakeIn = new ByteArrayInputStream("abc\n0\n".getBytes(StandardCharsets.UTF_8));
      System.setIn(fakeIn);
      // Main.main(new String[]{}) 호출 또는 루프 로직 추출 후 직접 테스트
      assertTrue(output().contains("메뉴는 숫자로 입력하세요."));
  }

  @Test
  void Main_0_입력_시_루프_정상_종료() {
      InputStream fakeIn = new ByteArrayInputStream("0\n".getBytes(StandardCharsets.UTF_8));
      System.setIn(fakeIn);
      // Main.main() 호출 후 무한루프 없이 종료되는지 검증
      // 타임아웃 어노테이션(@Timeout(value = 2, unit = TimeUnit.SECONDS)) 병행 권장
  }
  ```

---

## 검증 결과 요약

| 항목 | 결과 | 비고 |
|------|------|------|
| [A] 테스트 계획 존재 | 통과 | 통합 시나리오 6개, RouterTest 8개 전부 구현됨 |
| [B] 엣지케이스 식별 | 미흡 | Main EOF/닫힘 예외 및 루프 통합 흐름 테스트 부재 |
| [C] 기존 테스트 충돌 | 통과 | SampleControllerTest, SampleViewTest와 충돌 없음 |
| [D] 테스트 구조 | 미흡 | 설계 문서 output() 버그 및 outContent.reset() 누락 |
