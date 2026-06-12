# 테스트 전략 검증 보고서

**일시**: 2026-06-12  
**검증 대상**: docs/design/phase2.md (Phase 2 Model 구현)  
**결과**: ❌ 미흡 7건 (CRITICAL: 2, WARNING: 5)

---

## 발견된 문제

### [CRITICAL-1] Sample.java 커버리지 80% 미달 위험 — updateAvgProductionTime / updateStock 예외 경로 미테스트

- **대상 기능**: `Sample.updateAvgProductionTime()`, `Sample.updateStock()`
- **문제**: `SampleTest`에는 `updateYield(0.0)` 과 `updateName("")` 예외 케이스만 존재한다.
  `updateAvgProductionTime()` 과 `updateStock()` 은 각각 예외 분기(`< 1`, `< 0`)를 포함하지만
  단 한 건도 테스트되지 않았다. JaCoCo 기준으로 해당 메서드 본문 전체(6라인)가 미실행 상태이며,
  `toString()` 도 어떤 테스트에서도 직접 호출되지 않는다. Sample.java는 약 77라인으로,
  미커버 라인이 10라인 이상이면 커버리지가 80% 아래로 떨어진다.
  phase2.md 완료 기준에 `./gradlew jacocoTestReport` Model 커버리지 80% 이상이 명시되어 있으므로
  현 상태로는 완료 기준 미충족 위험이 있다.
- **권장 테스트 케이스 추가**:
  ```java
  @Test
  void updateAvgProductionTime_0_은_예외() {
      assertThrows(IllegalArgumentException.class,
              () -> sample.updateAvgProductionTime(0));
  }

  @Test
  void updateAvgProductionTime_1_은_정상() {
      sample.updateAvgProductionTime(1);
      assertEquals(1, sample.getAvgProductionTime());
  }

  @Test
  void updateStock_음수_는_예외() {
      assertThrows(IllegalArgumentException.class,
              () -> sample.updateStock(-1));
  }

  @Test
  void updateStock_0_은_정상() {
      sample.updateStock(0);
      assertEquals(0, sample.getStock());
  }

  @Test
  void toString_형식_검증() {
      Sample s = new Sample(1L, "AlphaChip", 30, 0.95, 100);
      assertTrue(s.toString().contains("AlphaChip"));
      assertTrue(s.toString().contains("30"));
  }
  ```

---

### [CRITICAL-2] findByNameContaining(null) 구현체 동작은 존재하나 테스트 미검증

- **대상 기능**: `InMemorySampleRepository.findByNameContaining()`
- **문제**: 구현체(라인 39)에서 `keyword == null || keyword.isBlank()` 조건으로 null 입력 시
  `findAll()` 을 반환한다. 그러나 phase2.md 5-3절 테스트 케이스 표에 null 케이스가 명시되지 않았고,
  `InMemorySampleRepositoryTest` 에도 해당 테스트가 없다. 구현과 테스트 간 불일치가 존재하며,
  Phase 3 Controller 에서 사용자 입력이 null 로 전달될 경우 동작 보장이 없다.
  JaCoCo 기준으로 `keyword == null` 분기(true 경로)가 미커버된다.
- **권장 테스트 케이스 추가**:
  ```java
  @Test
  void findByNameContaining_null_전체_반환() {
      repo.save(new Sample("AlphaChip", 30, 0.95, 100));
      List<Sample> result = repo.findByNameContaining(null);
      assertEquals(1, result.size());
  }
  ```
  또는 null 입력을 허용하지 않을 경우 설계 문서에 `NullPointerException` 또는
  `IllegalArgumentException` 발생을 명시하고, 구현체의 null 처리 분기를 제거한다.

---

### [WARNING-1] update* 메서드 정상 경로 전체 미테스트

- **대상 기능**: `Sample.updateName()`, `Sample.updateAvgProductionTime()`, `Sample.updateYield()`, `Sample.updateStock()`
- **문제**: `SampleTest` 에는 `updateYield(0.0)` 과 `updateName("")` 의 예외 경로만 있다.
  4개 update 메서드 모두에 대해 "유효한 값 전달 시 필드가 실제로 변경되는지" 검증하는 정상 경로
  테스트가 없다. 구현체가 값 갱신 없이 return 만 해도 현재 테스트로는 통과된다.
- **권장 테스트**:
  ```java
  @Test
  void updateName_정상_변경() {
      sample.updateName("BetaChip");
      assertEquals("BetaChip", sample.getName());
  }

  @Test
  void updateYield_정상_변경() {
      sample.updateYield(1.0);
      assertEquals(1.0, sample.getYield());
  }
  ```

---

### [WARNING-2] save() null 입력 및 update(null) 에 대한 NPE 위험 — 설계 및 테스트 미명시

- **대상 기능**: `InMemorySampleRepository.save()`, `InMemorySampleRepository.update()`
- **문제**: 구현체에서 `save(null)` 호출 시 `sample.getName()` (라인 15)에서 NPE 가 발생하고,
  `update(null)` 호출 시 `sample.getSampleId()` (라인 50)에서 NPE 가 발생한다.
  phase2.md 설계 문서 어디에도 이 입력에 대한 동작 명세가 없다.
  Phase 3 Controller 는 사용자 입력을 받아 save/update 를 호출하므로, null 방어가 어느 레이어에서
  이루어지는지 명확히 해야 한다.
- **권장 조치**: phase2.md 또는 phase3.md 에 "null Sample 입력은 Controller 레이어에서 방지한다"
  또는 "Repository 인터페이스 계약상 null 인수는 허용되지 않는다" 를 명시한다.
  인수가 null 일 경우 `IllegalArgumentException` 을 던지는 방어 코드를 추가하고 테스트로 검증하는
  것을 권장한다.

---

### [WARNING-3] findAll() 방어적 복사 동작 테스트 없음

- **대상 기능**: `InMemorySampleRepository.findAll()`
- **문제**: 구현체는 `new ArrayList<>(store)` 로 방어적 복사를 반환한다(라인 27).
  그러나 반환된 리스트를 수정해도 내부 `store` 가 변경되지 않는지 검증하는 테스트가 없다.
  설계 문서(phase2.md 4절 표)에도 이 동작 보장이 테스트 케이스로 명시되지 않았다.
  Phase 3 이후 Controller 나 View 가 반환 리스트를 수정할 경우 잠재적 버그 유입 경로가 된다.
- **권장 테스트**:
  ```java
  @Test
  void findAll_반환_리스트_수정이_저장소에_영향_없음() {
      repo.save(new Sample("AlphaChip", 30, 0.95, 100));
      List<Sample> result = repo.findAll();
      result.clear();
      assertEquals(1, repo.findAll().size());
  }
  ```

---

### [WARNING-4] updateName(null) 및 updateYield(1.1) 예외 케이스 미테스트

- **대상 기능**: `Sample.updateName()`, `Sample.updateYield()`
- **문제**: `updateName("")` 예외는 테스트되나 `updateName(null)` 은 테스트되지 않았다.
  `updateYield(0.0)` 예외는 테스트되나 `updateYield(1.1)` (상한 초과) 은 테스트되지 않았다.
  validate 로직과 update 메서드 내 검증 로직이 대칭적으로 작성되어 있으나,
  테스트 케이스가 비대칭이다.
- **권장 테스트**:
  ```java
  @Test
  void updateName_null_은_예외() {
      assertThrows(IllegalArgumentException.class,
              () -> sample.updateName(null));
  }

  @Test
  void updateYield_1_0_초과_는_예외() {
      assertThrows(IllegalArgumentException.class,
              () -> sample.updateYield(1.1));
  }
  ```

---

### [WARNING-5] Phase 3 진입 전 Mockito 의존성 추가 계획 미선행 확인

- **대상 기능**: `build.gradle`, `SampleControllerTest` (Phase 3 예정)
- **문제**: phase2.md 완료 기준에 `./gradlew test` 전 테스트 통과가 명시되어 있으나,
  현재 `build.gradle` 에 Mockito 의존성이 없다. `PLAN.md Phase 3-0` 에
  "Phase 3 구현 전 `build.gradle` 에 Mockito 추가" 가 명시되어 있으므로 Phase 2 단계에서의
  누락은 아니다. 그러나 Phase 3 진입 시 build.gradle 수정을 놓칠 경우
  `SampleControllerTest` 에서 컴파일 실패가 발생하므로, Phase 2 완료 체크리스트 또는 Phase 3
  설계 문서(phase3.md) 작업 목록 최상단에 이 항목을 명시적으로 포함해야 한다.
  현재 phase2.md 에는 해당 안내가 없다.
- **권장 조치**: phase2.md 6절 완료 기준에 "Phase 3 진입 전 `build.gradle` Mockito 의존성 추가
  확인" 항목을 추가하거나, phase3.md 작업 목록 첫 항목으로 재확인한다.

---

## 검증 결과 요약

- [A] 테스트 계획 존재: ✅ — phase2.md 5-2절(SampleTest 14개), 5-3절(Repository 12개) 케이스 전부 구현 파일에 1:1 대응 확인
- [B] 엣지케이스 식별: ❌ — updateAvgProductionTime/updateStock 경계값 미테스트(CRITICAL-1), findByNameContaining(null) 미테스트(CRITICAL-2), updateName(null)/updateYield(1.1) 미테스트(WARNING-4)
- [C] 기존 테스트 충돌: ✅ — SampleControllerTest/SampleViewTest/SampleFlowIntegrationTest 는 모두 placeholder 상태로 Phase 2 변경과 충돌 없음
- [D] 테스트 구조: ❌ — @BeforeEach 픽스처는 격리 올바름, 그러나 update* 정상 경로 미검증(WARNING-1), save/update null 처리 미명시(WARNING-2), findAll 방어적 복사 미검증(WARNING-3)
