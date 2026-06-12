# 테스트 전략 검증 보고서

**일시**: 2026-06-12
**검증 대상**: docs/design/phase5.md
**결과**: 미흡 5건 (CRITICAL: 2, WARNING: 3)

---

## 발견된 문제

### [CRITICAL] jacocoTestCoverageVerification includes 패키지 표기 불일치

- **대상 기능**: phase5.md §4 "JaCoCo 최소 커버리지 강제" Groovy 코드 블록
- **문제**: `includes` 목록에서 Model 패키지는 `'org.example.model.*'`(와일드카드 포함)로,
  Controller 패키지는 `'org.example.controller'`(와일드카드 없음)로 혼용 표기되어 있다.
  JaCoCo의 `includes`는 클래스 이름 패턴 매칭을 사용하는데, `'org.example.controller'`는
  패키지명 자체와 일치하려 하므로 `SampleController` 등 해당 패키지의 클래스들이
  coverage verification 대상에서 제외될 수 있다. 결과적으로 Controller 커버리지 80% 강제
  적용이 무효화될 위험이 있다.
- **권장 수정**: `includes` 표기를 `'org.example.controller.*'`로 통일한다.

```groovy
// 수정 예시
includes = ['org.example.model.*', 'org.example.controller.*']
```

---

### [CRITICAL] SampleFlowIntegrationTest 실제 케이스 수와 문서 명시 수 불일치

- **대상 기능**: phase5.md §3 테스트 통과 확인 표 — `SampleFlowIntegrationTest` 행
- **문제**: 설계 문서는 케이스 수를 6으로 명시하고 있으나, 실제
  `SampleFlowIntegrationTest.java`에는 8개 테스트 메서드가 존재한다.
  누락된 2건은 `Main_루프_숫자아닌_입력_오류_후_0으로_종료`와
  `Main_루프_정상_등록_후_0으로_종료`이다. 문서 기준으로 "전부 PASS" 여부를 판단할 때
  수치가 틀리면 실제 커버 범위를 오인하게 된다.
- **권장 수정**: 표의 `SampleFlowIntegrationTest` 케이스 수를 8로 정정한다.
  이후 테스트가 추가될 때 문서 수치를 동기화하는 것을 완료 기준 체크리스트에 포함한다.

---

### [WARNING] SampleController.register() — avgProductionTime 유효성 실패 시 printError 전달 검증 누락

- **대상 기능**: `SampleController.register()` — `IllegalArgumentException` catch 블록
- **문제**: `SampleControllerTest`에는 수율 `0.0` 입력 시 Model 예외가 Controller에서
  `view.printError()`로 위임되는지를 검증하는 케이스가 있다. 그러나 `avgProductionTime = 0`
  입력 시 동일한 경로(Model 생성자 예외 catch → `view.printError()`)가 올바르게 동작하는지를
  확인하는 케이스가 없다. phase5.md의 grep 검증 및 코드 리뷰 기준 어디에도 이 흐름이 언급되지
  않는다. 경계값 오류 메시지 위임 경로가 특정 필드에만 검증되고 있다.
- **권장 테스트**:
  ```java
  @Test
  void register_유효성_실패_avgProductionTime_0_저장없고_오류_출력() {
      controller("AlphaChip\n0\n0.95\n").register();
      assertTrue(repository.findAll().isEmpty());
      assertTrue(output().contains("[오류]"));
  }
  ```

---

### [WARNING] printSampleList(null) 전달 시 NullPointerException 가능성 — 검증 계획 없음

- **대상 기능**: `SampleView.printSampleList(List<Sample>)`
- **문제**: 구현체는 `samples.forEach(...)` 구조이므로 `null` 전달 시 NullPointerException이
  발생한다. phase5.md §2-3 View 체크 항목에는 `forEach` 루프를 "출력 반복만 수행 — 허용"으로
  명시하고 있으나, 그 루프에 null 리스트가 들어올 때의 동작은 검증 범위에 포함되어 있지 않다.
  Controller 구현상 `repository.findAll()`은 항상 리스트를 반환하므로 운용 중 null이 전달될
  가능성은 낮지만, View 레이어를 단독 단위 테스트할 때 방어 동작이 보장되지 않는다.
- **권장 조치**: `SampleViewTest`에 null 입력 케이스를 추가하거나, View 구현에 null 가드를 추가하고
  phase5.md §2-3 체크 항목에 "printSampleList null 입력 방어 여부 확인"을 명시한다.

---

### [WARNING] JaCoCo instrumentation과 비표준 ASCII classpath 교체 간 호환성 검증 절차 미명시

- **대상 기능**: phase5.md §1 JaCoCo 설정 배경 및 `test` 태스크 구성
- **문제**: 기존 `test` 태스크는 Windows CP949 경로 문제 우회를 위해 클래스를 ASCII 경로로
  복사하고 classpath를 교체하는 비표준 설정을 사용한다. JaCoCo 에이전트는 JVM 클래스 로딩 시
  바이트코드를 instrumenting하는데, ASCII classpath 교체 설정과 `classDirectories.setFrom(
  asciiMainClasses)` 조합이 실제로 올바르게 동작하는지에 대해 설계 문서는 "ASCII 경로 클래스를
  분석 대상으로 지정해야 리포트가 생성된다"고만 서술하고, 첫 실행 시 검증 절차를 명시하지 않는다.
  특히 `SampleControllerTest`, `SampleViewTest`, `SampleFlowIntegrationTest`가 사용하는
  `System.setOut()` 기반 출력 캡처가 instrumented 클래스 로딩 이후에도 동일하게 동작하는지
  별도 확인이 필요하다.
- **권장 조치**: §4 "JaCoCo 커버리지 측정" 절차 앞에 "첫 실행 후 HTML 리포트에 6개 테스트 클래스가
  모두 instrumented 클래스로 나타나는지 확인"하는 검증 단계를 추가한다.

---

## 검증 결과 요약

- [A] 테스트 계획 존재: 부분 미흡 — 기존 테스트 클래스 목록은 명시되어 있으나 SampleFlowIntegrationTest 케이스 수 오기재 (CRITICAL)
- [B] 엣지케이스 식별: 미흡 — Controller의 avgProductionTime=0 경계값 누락 (WARNING), View의 null 입력 미고려 (WARNING)
- [C] 기존 테스트 충돌: 부분 미흡 — 소스 변경 없어 직접 충돌 없음, JaCoCo instrumentation 타이밍 리스크 미검토 (WARNING)
- [D] 테스트 구조: 미흡 — jacocoTestCoverageVerification includes 패키지 와일드카드 불일치로 Controller 커버리지 강제 적용 무효화 위험 (CRITICAL)
