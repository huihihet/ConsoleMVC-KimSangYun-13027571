# 컴플라이언스 검증 보고서

**일시**: 2026-06-12  
**검증 대상**: `docs/design/phase3.md` — Controller · View 구현  
**결과**: ❌ 위반 3건 (CRITICAL: 1, WARNING: 2)

---

## 발견된 위반

### [CRITICAL] SampleControllerTest — searchByName 테스트 케이스 누락

- **위치**: `src/test/java/org/example/controller/SampleControllerTest.java` — 전체
- **위반 규칙**: `docs/design/phase3.md § 5 SampleControllerTest 테스트 케이스` 표에 `searchByName 결과 있음 → view.printSampleList() 호출` 및 `searchByName 결과 없음 → view.printEmpty() 호출` 2건이 필수 케이스로 명시되어 있음. `CLAUDE.md (ConsoleMVC)` 테스트 전략 항목에서 "Controller: Mock/Spy View 주입 후 흐름 검증", 목표 커버리지 80% 이상을 요구함.
- **현재 구현**: `SampleControllerTest`에 `searchByName` 관련 테스트 메서드가 전혀 존재하지 않음. `SampleController.searchByName()`은 구현되어 있으나 해당 분기(`printSampleList` 경로, `printEmpty` 경로) 모두 커버리지 미달 상태.
- **권장 수정**: 아래 두 케이스를 `SampleControllerTest`에 추가한다.
  ```java
  @Test
  void searchByName_결과_있음_시료_목록_출력() {
      repository.save(new Sample("AlphaChip", 30, 0.95, 100));
      controller("Alpha\n").searchByName();
      assertTrue(output().contains("AlphaChip"));
  }

  @Test
  void searchByName_결과_없음_안내_메시지_출력() {
      controller("없는이름\n").searchByName();
      assertTrue(output().contains("조회 결과가 없습니다."));
  }
  ```

---

### [WARNING] SampleView — printPrompt(), printUpdateFieldMenu() 설계 문서 메서드 목록 미반영

- **위치**: `docs/design/phase3.md` — § 2 SampleView 설계 > 메서드 목록 표
- **위반 규칙**: 설계 문서가 구현 스펙의 단일 진실 공급원이어야 하며, 루트 `과제/CLAUDE.md` "기능 추가·변경 전에 반드시 PRD·설계 문서를 먼저 수정한다" 규칙 적용.
- **현재 상태**: `SampleView`에 `printPrompt(String prompt)`, `printUpdateFieldMenu()` 두 메서드가 구현되어 있으나, `phase3.md § 2` 메서드 목록 표에는 해당 시그니처가 없음. `§ 3 update() 흐름 상세`에서 간접 언급되는 수준에 그침.
- **권장 수정**: `phase3.md § 2` 메서드 목록 표에 아래 행을 추가한다.
  ```markdown
  | `printPrompt(String prompt)` | 줄바꿈 없이 입력 안내 문자열 출력 |
  | `printUpdateFieldMenu()` | 수정 필드 선택 목록(1~4) 출력 |
  ```

---

### [WARNING] SampleView — printPrompt() 주석이 WHAT과 WHY를 혼용

- **위치**: `src/main/java/org/example/view/SampleView.java` — 24번 줄
- **위반 규칙**: `CLAUDE.md (ConsoleMVC)` 및 루트 `과제/CLAUDE.md` 공통 컨벤션 "주석: WHY가 비자명한 경우에만 한 줄 이내로 작성". WHAT 기술은 금지.
- **현재 구현**:
  ```java
  // 줄바꿈 없이 입력 프롬프트 출력 — Controller가 System.out 직접 호출 금지 규칙 준수
  public void printPrompt(String prompt) {
  ```
  "줄바꿈 없이 입력 프롬프트 출력"은 메서드명과 `System.out.print` 사용에서 자명한 WHAT이므로 컨벤션 위반. 뒷부분 WHY("Controller의 System.out 직접 호출 금지 규칙 준수")는 유효하나 혼용 자체가 위반임.
- **권장 수정**: WHAT 부분을 제거하고 WHY만 남긴다.
  ```java
  // Controller의 System.out 직접 호출 금지 규칙을 준수하기 위한 위임 메서드
  public void printPrompt(String prompt) {
  ```

---

## 검증 결과 요약

| 항목 | 결과 | 비고 |
|------|------|------|
| [A] 아키텍처 제약 | ✅ | 레이어 분리, 패키지 구조, 역방향 의존 없음 전부 준수 |
| [B] 코딩 컨벤션 | ❌ | 주석 WHAT/WHY 혼용 (WARNING 1건) |
| [C] 보안 | ✅ | 콘솔 PoC 범위 내 보안 이슈 없음 |
| [D] 불필요한 복잡성 | ✅ | 오버엔지니어링 및 불필요한 추상화 없음 |
| 테스트 완전성 | ❌ | searchByName 케이스 누락 (CRITICAL 1건), View 메서드 문서 불일치 (WARNING 1건) |

---

## 참고: 통과 항목

- Controller `System.out` 직접 호출: 없음. 모든 출력이 `view` 위임으로 처리됨.
- View 비즈니스 로직: 없음. `printPrompt()`, `printUpdateFieldMenu()` 포함 전 메서드가 순수 출력만 수행. `forEach` 람다는 반복 렌더링으로 비즈니스 로직으로 간주하지 않음.
- 레이어 역방향 의존: 없음. `SampleController`는 `model`, `view` import만 허용 범위 내에서 사용. `SampleView`는 `model.entity`만 참조하고 `controller` import 없음.
- View의 Model 직접 수정: 없음. `Sample` 객체는 읽기 전용 접근자(`getSampleId()` 등)만 호출.
- Constructor Injection: `SampleController(SampleRepository, SampleView, Scanner)` 생성자에서 전 의존성 주입.
- static 출력 메서드: 없음. `SEP` 상수만 `static final`이며 규칙이 금지하는 `static` 출력 메서드와 무관.
- 네이밍 컨벤션: `SampleView`, `SampleController` PascalCase; `printMenu`, `listAll`, `findById` 등 camelCase; `SEP` UPPER_SNAKE_CASE 모두 준수.
- build.gradle 외부 의존성: Mockito 5.x (`mockito-core`, `mockito-junit-jupiter`) 외 추가 없음.
