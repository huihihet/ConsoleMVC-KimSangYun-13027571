# 테스트 전략 검증 보고서

**일시**: 2026-06-12  
**검증 대상**: docs/design/phase3.md (Phase 3 Controller·View 구현)  
**결과**: ❌ 미흡 7건 (CRITICAL: 3, WARNING: 4)

---

## 발견된 문제

### [CRITICAL-1] searchByName 테스트 2건 전부 누락 — 설계 14개 중 2개 미구현

- **대상 기능**: `SampleController.searchByName()`
- **문제**: phase3.md 5절 SampleControllerTest 케이스 표에 "searchByName 결과 있음 — view.printSampleList() 호출"과
  "searchByName 결과 없음 — view.printEmpty() 호출" 두 케이스가 명시되어 있으나,
  SampleControllerTest에 해당 테스트가 단 하나도 존재하지 않는다.
  `searchByName()`은 F-06 기능이며, Controller 내에 두 개의 분기(`list.isEmpty()` 여부)가 있는데
  두 분기 모두 런타임까지 검증 없이 진행된다.
- **권장 테스트 케이스 추가**:
  ```java
  @Test
  void searchByName_결과_있으면_목록_출력() {
      repository.save(new Sample("AlphaChip", 30, 0.95, 100));
      controller("Alpha\n").searchByName();
      assertTrue(output().contains("AlphaChip"));
  }

  @Test
  void searchByName_결과_없으면_안내_메시지_출력() {
      controller("없는이름\n").searchByName();
      assertTrue(output().contains("조회 결과가 없습니다."));
  }
  ```

---

### [CRITICAL-2] update() 필드 선택 케이스 2·3·4번 및 default 미테스트

- **대상 기능**: `SampleController.update()` — case "2"(생산시간), case "3"(수율), case "4"(재고), default
- **문제**: update 정상 경로 테스트는 case "1"(이름)만 작성되어 있다.
  case "2", "3", "4"는 각각 `Integer.parseInt` / `Double.parseDouble` 파싱과
  `IllegalArgumentException` 두 가지 오류 분기를 추가로 포함하므로, 이 케이스들을 테스트하지 않으면
  해당 분기 전체가 미커버 상태가 된다.
  default 케이스(유효하지 않은 필드 번호 입력 시 printError 호출)도 테스트 케이스가 없다.
  PLAN.md Phase 5 완료 기준에 "Model·Controller 커버리지 80% 이상"이 명시되어 있어,
  현 상태로는 update() 커버리지가 기준 미달 위험이 있다.
- **권장 테스트 케이스 추가**:
  ```java
  @Test
  void update_생산시간_정상_수정_저장소에_반영() {
      repository.save(new Sample("AlphaChip", 30, 0.95, 100));
      // ID=1, 필드선택=2(생산시간), 새값=60
      controller("1\n2\n60\n").update();
      assertEquals(60, repository.findById(1L).get().getAvgProductionTime());
  }

  @Test
  void update_수율_정상_수정_저장소에_반영() {
      repository.save(new Sample("AlphaChip", 30, 0.95, 100));
      controller("1\n3\n0.80\n").update();
      assertEquals(0.80, repository.findById(1L).get().getYield(), 0.001);
  }

  @Test
  void update_재고_정상_수정_저장소에_반영() {
      repository.save(new Sample("AlphaChip", 30, 0.95, 100));
      controller("1\n4\n200\n").update();
      assertEquals(200, repository.findById(1L).get().getStock());
  }

  @Test
  void update_유효하지않은_필드_선택_오류_출력() {
      repository.save(new Sample("AlphaChip", 30, 0.95, 100));
      controller("1\n9\n").update();
      assertTrue(output().contains("[오류]"));
  }

  @Test
  void update_생산시간_파싱_실패_오류_출력() {
      repository.save(new Sample("AlphaChip", 30, 0.95, 100));
      controller("1\n2\nabc\n").update();
      assertTrue(output().contains("[오류]"));
  }

  @Test
  void update_수율_파싱_실패_오류_출력() {
      repository.save(new Sample("AlphaChip", 30, 0.95, 100));
      controller("1\n3\nabc\n").update();
      assertTrue(output().contains("[오류]"));
  }

  @Test
  void update_재고_파싱_실패_오류_출력() {
      repository.save(new Sample("AlphaChip", 30, 0.95, 100));
      controller("1\n4\nabc\n").update();
      assertTrue(output().contains("[오류]"));
  }
  ```

---

### [CRITICAL-3] @AfterEach의 System.out 복원 로직이 원래 stdout을 보장하지 않음

- **대상 기능**: `SampleViewTest.tearDown()`, `SampleControllerTest.tearDown()`
- **문제**: 두 테스트 클래스 모두 `@AfterEach`에서 `System.setOut(System.out)`을 호출한다.
  그러나 `@BeforeEach`에서 이미 `System.setOut(new PrintStream(outContent))`이 실행된 이후에는
  `System.out` 필드가 새 PrintStream을 참조하므로,
  `System.setOut(System.out)`은 리다이렉트된 스트림으로 복원하는 것과 같다.
  즉, 원래의 JVM 표준 출력이 복원되지 않는다.
  단일 테스트 실행 시에는 JVM 초기 `System.out` 참조가 이미 사라진 상태이므로,
  테스트 종료 후 콘솔 출력이 정상적으로 동작하지 않거나
  이후 테스트에서 캡처 스트림이 재사용되는 상황이 발생할 수 있다.
- **권장 수정 패턴**:
  ```java
  // 필드 추가
  private PrintStream originalOut;

  @BeforeEach
  void setUp() {
      originalOut = System.out;  // 교체 전에 원본 저장
      outContent = new ByteArrayOutputStream();
      System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));
      view = new SampleView();
  }

  @AfterEach
  void tearDown() {
      System.setOut(originalOut);  // 원본으로 복원
  }
  ```
  이 패턴은 SampleViewTest와 SampleControllerTest 양쪽 모두에 적용해야 한다.

---

### [WARNING-1] 설계 외 추가된 printPrompt()·printUpdateFieldMenu() 메서드 직접 테스트 없음

- **대상 기능**: `SampleView.printPrompt(String)`, `SampleView.printUpdateFieldMenu()`
- **문제**: phase3.md 설계 표에는 6개 메서드만 명시되어 있으나, 구현에서 두 메서드가 추가되었다.
  SampleViewTest에는 이 두 메서드를 직접 호출하여 출력 내용을 검증하는 테스트가 없다.
  SampleControllerTest에서 update() 실행 중 `printUpdateFieldMenu()`가 간접 호출되기는 하지만,
  출력 문자열("1. 이름  2. 평균 생산 시간  3. 수율  4. 재고")을 assert하는 코드가 없어
  포맷 오류가 발생해도 탐지되지 않는다.
- **권장 테스트 케이스 추가** (SampleViewTest):
  ```java
  @Test
  void printPrompt_입력된_텍스트_출력() {
      view.printPrompt("이름: ");
      assertTrue(output().contains("이름: "));
  }

  @Test
  void printUpdateFieldMenu_필드_목록_출력() {
      view.printUpdateFieldMenu();
      String out = output();
      assertTrue(out.contains("이름"));
      assertTrue(out.contains("수율"));
      assertTrue(out.contains("재고"));
  }
  ```

---

### [WARNING-2] update()의 ID 파싱 실패 케이스 미테스트

- **대상 기능**: `SampleController.update()` — ID `NumberFormatException` 분기
- **문제**: `findById()`와 `delete()`에는 ID 파싱 실패 테스트가 각각 존재하지만,
  `update()`에는 동일한 분기(ID 입력에 문자열 전달 시 printError 호출)에 대한 테스트가 없다.
  설계 문서 phase3.md의 "입력 파싱 규칙" 절에 "Long 파싱 실패 → view.printError() 호출 후 종료"가
  명시되어 있으므로, update() 역시 동일 기준을 적용해야 한다.
- **권장 테스트 케이스 추가**:
  ```java
  @Test
  void update_ID_파싱_실패_오류_출력() {
      controller("abc\n").update();
      assertTrue(output().contains("[오류]"));
      // repository 변경 없음 확인
      assertTrue(repository.findAll().isEmpty());
  }
  ```

---

### [WARNING-3] delete()의 ID 파싱 실패 케이스 미테스트

- **대상 기능**: `SampleController.delete()` — ID `NumberFormatException` 분기
- **문제**: `delete()` 구현(SampleController.java 167~175라인)에는 ID 파싱 실패 시
  `view.printError("ID는 숫자로 입력하세요.")`를 호출하는 분기가 있지만,
  SampleControllerTest에 해당 케이스가 없다.
- **권장 테스트 케이스 추가**:
  ```java
  @Test
  void delete_ID_파싱_실패_오류_출력() {
      controller("abc\n").delete();
      assertTrue(output().contains("[오류]"));
  }
  ```

---

### [WARNING-4] Phase 4 진입 전 추가로 고려해야 할 엣지케이스

- **대상 기능**: `SampleController.searchByName()`, `SampleController.register()`, Phase 4 통합 흐름
- **문제 1 — searchByName 빈 문자열 입력**: Phase 2 InMemorySampleRepository 설계에
  "findByNameContaining 빈 문자열 → 전체 반환"이 명시되어 있으나,
  Controller 레벨에서 이 케이스를 테스트하는 코드가 없다.
  사용자가 검색어 없이 엔터를 입력하면 전체 목록이 출력되어야 하는지, 아니면 빈 결과로 처리해야 하는지
  Phase 4 통합 전에 명확히 해야 한다.
- **문제 2 — register() 재고 초기값 0 고정**: 구현에서 `new Sample(name, avgProductionTime, yield, 0)`으로
  재고를 0으로 고정하지만, phase3.md에 이 결정이 명시되어 있지 않다.
  Phase 4의 출고·생산 흐름에서 재고 0인 시료가 기본 상태가 되면 시나리오 테스트가 복잡해진다.
  Phase 4 진입 전에 재고 초기값 입력 요구 여부를 설계 문서에 명시할 것을 권장한다.
- **문제 3 — update() 유효성 검증 실패 후 repository 상태 검증 없음**: update 케이스들 중
  수율 범위 위반(0.0 이하, 1.0 초과) 입력 시 repository가 변경되지 않는지 확인하는 assertion이 없다.
  현재 구현은 `sample.updateYield()` 내부에서 예외를 던지므로 `repository.update()`에 도달하지 않지만,
  이를 명시적으로 검증하는 테스트가 있어야 리팩토링 안전망이 확보된다.

---

## 검증 결과 요약

- [A] 테스트 계획 존재: ❌ — 설계 명시 14개 케이스 중 searchByName 2건 미구현 (CRITICAL-1)
- [B] 엣지케이스 식별: ❌ — update() ID 파싱 실패·필드 2~4·default 미테스트 (CRITICAL-2, WARNING-2, WARNING-3), 설계 외 추가 메서드 미검증 (WARNING-1)
- [C] 기존 테스트 충돌: ✅ — Phase 2 SampleTest·InMemorySampleRepositoryTest와 충돌 없음, SampleFlowIntegrationTest는 placeholder 유지
- [D] 테스트 구조: ❌ — @AfterEach System.out 복원 로직이 원래 stdout을 보장하지 않음 (CRITICAL-3), Phase 4 대비 엣지케이스 미명시 (WARNING-4)
