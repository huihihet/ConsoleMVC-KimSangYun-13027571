# 문서 정합성 검증 보고서

**일시**: 2026-06-12
**검증 문서**:
- `과제/CLAUDE.md` (루트 규칙)
- `과제/ConsoleMVC/CLAUDE.md` (서브 프로젝트 규칙)
- `과제/ConsoleMVC/docs/PRD.md` (요구사항)
- `과제/ConsoleMVC/docs/PLAN.md` (구현 계획)

**결과**: ❌ 문제 4건 발견 (CRITICAL: 0, WARNING: 2, INFO: 2)

---

## 발견된 문제

### [WARNING-1] JUnit 버전 표기 불일치 — 루트 CLAUDE.md vs 하위 문서

- **위치**: `과제/CLAUDE.md` — "기술 스택" 섹션
- **설명**: 루트 CLAUDE.md는 테스트 프레임워크를 `JUnit Jupiter 5.x (또는 6.x)`로 기술하여 두 버전을 모두 허용한다. 반면 `ConsoleMVC/CLAUDE.md`와 `PLAN.md Phase 1`은 `JUnit Jupiter 6.x`로 버전을 단정한다. 루트 문서에 `5.x`가 여전히 병기되어 있어, 다른 PoC 프로젝트에서 5.x를 선택하면 동일 과제 내 버전이 혼재하는 상황이 발생할 수 있다.
- **권장 조치**: 루트 CLAUDE.md의 테스트 항목을 `JUnit Jupiter 6.x`로 단일화하거나, 하위 CLAUDE.md에 "루트의 '5.x 또는 6.x' 중 ConsoleMVC는 6.x 채택"임을 명시하여 의도적 선택임을 문서화한다.

---

### [WARNING-2] Router 생성자의 View 직접 주입 — 역할 분리 규칙 위반 소지

- **위치**: `PLAN.md` — Phase 4, 4-2 Main 조립 코드 스니펫
- **설명**: PLAN.md는 `Router`를 `new Router(controller, view)`로 조립하도록 설계한다. 그런데 `ConsoleMVC/CLAUDE.md` 및 루트 CLAUDE.md의 역할 분리 규칙에 따르면 `View`로의 접근 경로는 반드시 `Controller`를 통해야 하며(`Controller → View`), `Router`가 `SampleView`를 직접 보유하여 출력 메서드를 호출하면 이 의존 방향을 우회하게 된다. PRD의 클래스 책임 표에서도 `Router`의 책임은 "메뉴 번호 → Controller 메서드 라우팅"으로만 정의되어 있어 View 보유 근거가 없다.
- **권장 조치**: `Router`에서 View 의존성을 제거하고, 잘못된 메뉴 번호 처리(오류 메시지 출력)는 `SampleController`에 전용 메서드(예: `handleInvalidMenu()`)를 두거나 `Router`가 `SampleController.handleInvalidMenu()`를 호출하도록 설계를 수정한다.

---

### [INFO-1] F-07 메뉴 네비게이션의 PLAN 내 명시적 구현 매핑 부재

- **위치**: `PLAN.md` — Phase 3, 3-2 SampleController 메서드 목록
- **설명**: PRD는 F-01~F-07 총 7개 기능을 정의한다. PLAN Phase 3의 `SampleController` 메서드 목록은 F-01~F-06만 명시하고 F-07(메뉴 네비게이션)은 어느 클래스·Phase에서 구현되는지 서술하지 않는다. `Router`와 `Main`에서 F-07이 처리됨을 코드 스니펫으로 추론할 수 있으나, PRD 요구사항 ID와 PLAN 구현 위치 간의 명시적 추적(traceability)이 불완전하다.
- **권장 조치**: Phase 4 설명 첫 줄 또는 Router 설명에 "F-07 메뉴 네비게이션 담당"임을 한 줄 추가하여 PRD-PLAN 간 추적성을 확보한다.

---

### [INFO-2] PRD 마일스톤(M1~M5)과 PLAN Phase 번호 간 명시적 연결 표기 부재

- **위치**: `PLAN.md` — 각 Phase 헤더 / `PRD.md` — 섹션 8 마일스톤
- **설명**: PRD 섹션 8은 M1~M5 마일스톤을 정의하고, PLAN은 Phase 1~5로 구성되어 1:1 대응 구조를 갖는다. 그러나 PLAN의 각 Phase 헤더에 "PRD M1 대응", "PRD M2 대응" 등의 역참조 표기가 없어, 문서 간 연결을 독자가 직접 유추해야 한다.
- **권장 조치**: 각 Phase 헤더 아래에 `> PRD 마일스톤: M{N}` 한 줄을 추가하여 추적성을 확보한다.

---

## 통과 항목

- **[A] 교차 참조**: PLAN.md의 기준 문서 링크(`docs/PRD.md`) 실제 경로 일치. Phase 1~5 구조가 PRD M1~M5에 대응. 이상 없음
- **[B] 기술 스택**: Java 17+, Gradle 8.x — 세 문서 모두 일치. JUnit 버전 표기 불일치 1건 (WARNING-1로 별도 기록)
- **[C] 설계 제약 반영**: ConsoleMVC PoC 범위(DB·파일 영속성 제외, 인메모리 저장소 사용) PLAN에 정확히 반영됨. Router-View 의존 설계 위반 소지 1건 (WARNING-2로 별도 기록)
- **[D] 완료 기준**: Phase 1~5 전체에 완료 기준 체크리스트 존재. 이상 없음
- **[E] 내부 모순**: PRD F-01~F-06 전 기능이 PLAN Controller 메서드에 1:1 대응. 상위-하위 CLAUDE.md 간 PoC 범위 제한(외부 의존성 없음)은 루트의 "최소화" 방침 내 정상 구체화. 이상 없음
- **[F] 도메인 엔티티**: PRD Sample 엔티티 5개 필드(sampleId, name, avgProductionTime, yield, stock)가 PLAN Phase 2 Sample 클래스 설계에 모두 반영됨. 이상 없음
- **[G] 수용 기준 연결**: PRD 수용 기준 8개 항목이 PLAN Phase 3·4·5 완료 기준에 분산 반영됨. Phase 5에 "PRD 수용 기준 전 항목 충족" 체크 항목 존재. 이상 없음
