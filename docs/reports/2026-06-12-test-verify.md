# 테스트 전략 검증 보고서

**일시**: 2026-06-12  
**검증 대상**: docs/design/phase1.md (Phase 1 스켈레톤 구현)  
**결과**: ❌ 미흡 6건 (CRITICAL: 1, WARNING: 5)

---

## 발견된 문제

### [CRITICAL-1] Phase 3 Controller 테스트에서 필요한 Mockito 의존성 계획 누락

- **대상 기능**: `SampleControllerTest` (Phase 3 구현 예정)
- **문제**: `CLAUDE.md` 및 `PLAN.md Phase 3-3`은 `SampleControllerTest`에서 "Spy/Mock `SampleView` 주입"을 명시한다. 그러나 `build.gradle`에는 Mockito 의존성이 없고, 어느 phase 설계 문서에도 Mockito(또는 대안 Mock 라이브러리) 추가 시점이 기술되어 있지 않다. JUnit Jupiter 6.x는 Mockito 자동 통합을 제공하지 않으므로, Phase 3에서 테스트를 작성하는 시점에 `build.gradle` 수정이 필요해지는데 그 계획이 없다. 현재 Phase 1 `build.gradle`이 완료 상태로 간주되면 Phase 2~3 설계 문서에서 이 누락이 발견되지 않고 넘어갈 위험이 크다.
- **권장 테스트 / 조치**:
  - Phase 2 또는 Phase 3 설계 문서에 `build.gradle` 의존성 추가 항목을 명시한다.
    ```groovy
    testImplementation 'org.mockito:mockito-core:5.x'
    testImplementation 'org.mockito:mockito-junit-jupiter:5.x'
    ```
  - 또는 Mockito 없이 수동 Spy(익명 서브클래스 또는 `ByteArrayOutputStream` 캡처)로 대체한다면, `SampleViewTest`에 이미 사용되는 `ByteArrayOutputStream` 방식으로 통일함을 명시한다.

---

### [WARNING-1] SampleFlowIntegrationTest 패키지가 main 미러링 규칙 위반

- **대상 기능**: `SampleFlowIntegrationTest`
- **문제**: `CLAUDE.md`는 "src/test/java/org/example/ — main과 미러링된 패키지 구조"를 규정한다. 그런데 `SampleFlowIntegrationTest`는 `org.example.integration` 패키지에 위치하며, `src/main/java/org/example/` 하위에 `integration/` 패키지는 존재하지 않는다. 이는 미러링 원칙의 예외로, 규칙 위반 또는 의도적 예외 중 어느 쪽인지 문서에 명시되어 있지 않다.
- **권장 조치**:
  - 통합 테스트는 특성상 특정 main 패키지와 1:1 대응이 어렵다. `CLAUDE.md` 또는 phase1.md 섹션 3에 "통합 테스트는 `org.example.integration` 패키지에 예외적으로 위치한다"는 한 줄 주석을 추가하여 의도적 예외임을 명확히 한다.

---

### [WARNING-2] Phase 2 엣지케이스 중 경계 정확값과 공백 입력 케이스 누락

- **대상 기능**: `SampleTest`, `InMemorySampleRepositoryTest` (Phase 2 구현 예정)
- **문제**: `PLAN.md Phase 2-4` 단위 테스트 표에는 "수율 범위 위반", "생산 시간 0 이하", "재고 음수" 케이스가 있으나, 다음 경계값 케이스가 누락되어 있다.
  1. `yield = 0.0` (경계값 — 0.0은 허용인지 불허인지 정의 불명확: PRD는 "0.0 초과"로 명시하나 테스트 케이스에 반영 안 됨)
  2. `yield = 1.0` (상한 경계값 — 허용이나 테스트 케이스 없음)
  3. `name`이 공백 문자열(`""`) 또는 공백만으로 구성된 문자열(`"   "`) 입력 (PRD는 "공백 불허"로 명시)
  4. `findByNameContaining("")` — 빈 문자열 키워드 전달 시 전체 반환 여부
- **권장 테스트**: Phase 2 설계 문서 테스트 케이스 표에 다음 4건을 추가한다.
  ```
  | yield = 0.0 입력          | IllegalArgumentException 발생 |
  | yield = 1.0 입력          | 정상 생성 (상한 포함 허용 확인) |
  | name = "" 또는 "   " 입력 | IllegalArgumentException 발생 |
  | findByNameContaining("") | 전체 목록 반환 또는 빈 목록 — 명세 결정 후 테스트 |
  ```

---

### [WARNING-3] Phase 2 구현 시 InMemorySampleRepository 컴파일 변경으로 인한 Phase 1 placeholder 테스트 깨짐 위험 — regression 계획 없음

- **대상 기능**: `InMemorySampleRepositoryTest`, `SampleRepository` 인터페이스
- **문제**: 현재 `SampleRepository` 인터페이스는 메서드 없이 빈 상태이고, `InMemorySampleRepository`는 이를 구현한다. Phase 2에서 `SampleRepository`에 `save`, `findAll` 등 6개 메서드를 추가하면 `InMemorySampleRepository`에 즉시 미구현 메서드가 생겨 컴파일이 실패하고, Phase 1 placeholder 테스트 전체가 깨진다. 이 전환 시점의 regression 처리 계획(예: Phase 2 작업 착수 전 placeholder 교체 절차)이 설계 문서에 없다.
- **권장 조치**: Phase 2 설계 문서 작업 목록 첫 항목으로 "기존 placeholder 테스트를 Phase 2 케이스로 교체 후 컴파일 확인"을 명시한다. 또는 Phase 1 완료 기준에 "Phase 2 시작 전 SampleRepository 인터페이스는 비어 있어야 함"을 체크리스트에 추가한다.

---

### [WARNING-4] Phase 2 단위 테스트용 공통 픽스처(@BeforeEach) 계획 부재

- **대상 기능**: `SampleTest`, `InMemorySampleRepositoryTest`
- **문제**: `PLAN.md Phase 2-4`의 10개 테스트 케이스 중 "save → findById", "findAll", "findByNameContaining", "update", "deleteById" 5개는 사전에 저장된 `Sample` 객체를 전제한다. 그러나 설계 문서에는 각 테스트가 공유할 `Sample` 인스턴스 생성 픽스처(`@BeforeEach`)에 대한 언급이 없다. 픽스처 계획이 없으면 Phase 2 구현자가 각 테스트마다 중복 초기화 코드를 작성하거나, 상태 공유로 인한 테스트 격리 오염이 발생할 수 있다.
- **권장 테스트**: Phase 2 설계 문서에 다음 픽스처 패턴을 명시한다.
  ```java
  private InMemorySampleRepository repo;

  @BeforeEach
  void setUp() {
      repo = new InMemorySampleRepository(); // 매 테스트마다 새 인스턴스 — 격리 보장
  }
  ```

---

### [WARNING-5] placeholder 메서드에서 Phase별 확장 시 메서드 교체 vs. 추가 방식 미명시

- **대상 기능**: 전체 5개 테스트 클래스의 `placeholder()` 메서드
- **문제**: 각 테스트 클래스의 `placeholder()` 메서드는 `// Phase {N}에서 구현` 주석만 있고, 향후 Phase에서 이 메서드를 삭제하고 실질 테스트 메서드를 추가하는 것인지, 아니면 `placeholder()` 안에 assertions를 채우는 것인지 명시되어 있지 않다. 특히 `InMemorySampleRepositoryTest`는 Phase 2에서 10개 케이스가 생기므로 단일 `placeholder()` 메서드로 수용 불가하다. 명시 없이는 Phase 2~4 구현 시 일관성 없는 구조가 될 수 있다.
- **권장 조치**: phase1.md 섹션 5 또는 각 테스트 클래스 주석에 "Phase {N} 구현 시 `placeholder()` 메서드를 삭제하고 해당 Phase의 테스트 케이스 메서드를 추가한다"는 한 줄 지침을 추가한다.

---

## 검증 결과 요약

- [A] 테스트 계획 존재: ✅ — Phase 1 완료 기준에 `./gradlew test` 명시, 5개 테스트 클래스 모두 생성됨
- [B] 엣지케이스 식별: ❌ — yield 경계 정확값(0.0/1.0), name 공백, findByNameContaining 빈 문자열 케이스 누락 (WARNING-2)
- [C] 기존 테스트 충돌: ❌ — Phase 2 인터페이스 확장 시 placeholder 테스트 컴파일 실패 위험, regression 계획 없음 (WARNING-3)
- [D] 테스트 구조: ❌ — 통합 테스트 패키지 미러링 원칙 위반 미명시 (WARNING-1), 픽스처 계획 부재 (WARNING-4), placeholder 확장 지침 없음 (WARNING-5), Mockito 의존성 계획 누락 (CRITICAL-1)
