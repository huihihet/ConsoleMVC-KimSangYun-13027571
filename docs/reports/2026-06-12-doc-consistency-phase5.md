# 문서 정합성 검증 보고서

**일시**: 2026-06-12  
**검증 문서**:
- `docs/PLAN.md`
- `docs/PRD.md`
- `docs/design/phase5.md` (신규 생성)
- `ConsoleMVC/CLAUDE.md`
- `과제/CLAUDE.md` (부모)
- `docs/design/phase1.md` ~ `phase4.md` (구조적 모순 교차 검증)

**결과**: ❌ 문제 4건 발견 (CRITICAL: 0, WARNING: 3, INFO: 1)

---

## 발견된 문제

### [WARNING-1] PRD §5 비기능 요구사항 "JUnit 외 추가 라이브러리 없음" — Mockito 추가로 위반

- **위치**: `docs/PRD.md` §5 비기능 요구사항
- **설명**: PRD §5에 "외부 의존성: JUnit 외 추가 라이브러리 없음"으로 명시되어 있다.
  그러나 `docs/design/phase3.md` §1 선행 작업에서 Mockito 5.x (`mockito-core`, `mockito-junit-jupiter`)를
  `build.gradle`에 추가하는 설계가 포함되어 있으며, `PLAN.md Phase 3-0`에도 동일하게 기술되어 있다.
  phase5.md는 이 상충을 해소하지 않은 채 검증 단계에 진입한다.
- **영향**: phase5.md PRD §7 수용 기준 최종 점검 표의 검증 대상에 "외부 의존성 JUnit 외 추가 라이브러리 없음" 항목이 없어 PRD §5 위반이 최종 검증에서 누락될 위험이 있다.
- **권장 조치**: PRD §5 비기능 요구사항을 "JUnit 및 Mockito(testImplementation 한정) 외 추가 라이브러리 없음"으로 수정하거나, phase5.md §5 점검 표에 "Mockito는 testImplementation 범위 한정 사용 — main 코드에 Mockito 의존성 없음" 항목을 추가한다.

---

### [WARNING-2] PLAN.md 파일 생성 체크리스트에 `RouterTest.java` 누락

- **위치**: `docs/PLAN.md` — "파일 생성 체크리스트 (전체)" 섹션
- **설명**: PLAN.md의 전체 파일 생성 체크리스트(`src/test/java/org/example/` 하위)에
  `RouterTest.java`가 포함되어 있지 않다.
  반면 `docs/design/phase4.md` §5 파일 목록에는
  `src/test/java/org/example/app/RouterTest.java` 신규 생성이 명시되어 있으며,
  `phase5.md` §3 테스트 케이스 표에도 `RouterTest | 8케이스 | 전부 PASS` 항목이 포함되어 있다.
- **영향**: PLAN.md 체크리스트 기준으로 프로젝트 완료 여부를 판단할 때 RouterTest 파일이 누락된 것으로 보일 수 있다.
- **권장 조치**: `docs/PLAN.md` 파일 생성 체크리스트 `src/test/java/org/example/` 하위에
  `app/RouterTest.java` 항목을 추가한다.

---

### [WARNING-3] phase2.md 완료 기준에 `./gradlew jacocoTestReport` 선행 언급 — JaCoCo 설정은 phase5에서 추가

- **위치**: `docs/design/phase2.md` §6 완료 기준 (211번 줄)
- **설명**: phase2.md 완료 기준에 `./gradlew jacocoTestReport Model 커버리지 80% 이상` 항목이 포함되어 있다.
  그러나 JaCoCo 플러그인 및 `jacocoTestReport` 태스크 설정은 `phase5.md` §1에서 처음 `build.gradle`에 추가되는 설계다.
  phase2 시점에는 JaCoCo가 아직 설정되어 있지 않으므로 해당 완료 기준은 phase2 단계에서 충족이 불가능하다.
- **영향**: phase2 완료 기준이 현실과 불일치하여 phase2 완료 판단에 혼선을 줄 수 있다.
- **권장 조치**: phase2.md §6의 `./gradlew jacocoTestReport` 항목을 삭제하거나
  `(Phase 5에서 검증)`이라는 주석을 추가해 선행 조건임을 명시한다.
  또는 JaCoCo 설정을 phase2나 phase1으로 앞당겨 설정 시점을 일치시킨다.

---

### [INFO-1] phase5.md §1 JaCoCo `toolVersion = "0.8.12"` 버전 — 상위 문서에 명시 없음

- **위치**: `docs/design/phase5.md` §1 `build.gradle` JaCoCo 설정 추가
- **설명**: phase5.md에서 `jacoco { toolVersion = "0.8.12" }`를 명시하고 있으나,
  `CLAUDE.md`(부모·자식 모두) 및 `PLAN.md`에는 JaCoCo 버전에 대한 언급이 없다.
  버전 고정은 재현성 측면에서 권장되나, 상위 문서에 근거 없이 특정 버전이 설계 문서에만 등장한다.
- **영향**: 향후 JaCoCo 버전을 변경해야 할 경우 phase5.md만 수정하면 되나, 버전 결정 근거가 문서화되지 않는다.
- **권장 조치**: `ConsoleMVC/CLAUDE.md` 기술 스택 섹션에 `JaCoCo 0.8.x` 항목을 추가하거나,
  phase5.md 내 주석으로 버전 선택 이유(예: Java 21+ 호환 최신 안정 버전)를 한 줄 기술한다.

---

## 통과 항목

### [A] 교차 참조 일관성
- PLAN.md Phase 1~5 정의 ↔ `phase1.md`~`phase5.md` 파일 존재: **일치**
- phase5.md 헤더 기준 문서 표기 `docs/PLAN.md > Phase 5` 및 `PRD 마일스톤: M5`: **일치**
- phase5.md §5 내부 참조(`§2-1`, `§2-2`, `§2-3`): **해당 섹션 존재 확인**
- **결과**: 문제 0건 (RouterTest 누락은 [WARNING-2]로 별도 기록)

### [B] 기술 스택 일관성 — JUnit
- `ConsoleMVC/CLAUDE.md`: JUnit Jupiter **6.x**
- `phase1.md` `build.gradle` 설계: `junit-bom:6.0.0` — **일치**
- `phase5.md`: JUnit 버전 별도 언급 없음 (phase1 설정 유지) — **이상 없음**
- 부모 `과제/CLAUDE.md`: "5.x (또는 6.x)"로 범위 허용 — **상충 없음**
- **결과**: 이상 없음

### [B] 기술 스택 일관성 — Java 17 / Gradle 8.x
- phase5.md는 Java 버전·Gradle 버전을 직접 명시하지 않으며 기존 빌드 설정을 상속 — **이상 없음**
- **결과**: 이상 없음

### [C] 설계 제약 반영
- `ConsoleMVC/CLAUDE.md` 역할 분리 규칙(Controller `System.out` 금지, Model I/O 금지, View 로직 금지):
  phase5.md §2에서 동일 규칙을 grep 검증 대상으로 명시 — **반영 확인**
- "수정 금지" 등 별도 제약 없음 — **해당 없음**
- **결과**: 이상 없음

### [D] 완료 기준 누락
- phase5.md §7 완료 기준 8개 항목 명시 — **누락 없음**
- PLAN.md Phase 5 체크리스트 6개 항목이 phase5.md §7에 모두 포함(phase5.md가 2개 항목 추가) — **상위 집합으로 충족**
- **결과**: 이상 없음

### [E] 내부 모순
- **검증 항목 1** — phase5.md 목표·완료 기준 vs. PLAN.md Phase 5: 일치 확인. PLAN.md의 "역할 분리 규칙 준수 여부 확인 + 커버리지 측정" 목표가 phase5.md에 동일하게 반영됨.
- **검증 항목 2** — PRD §7 수용 기준 8개 vs. phase5.md §5 점검 표 8개: 항목 번호 1:1 대응 확인. WARNING-1에서 언급한 Mockito 관련 항목을 제외하면 전 항목 반영.
- **검증 항목 4** — phase1~4 설계 문서와 구조적 모순: 클래스명(`Sample`, `SampleController`, `SampleView`, `InMemorySampleRepository`, `Router`), 패키지(`org.example.*`), 생성자 주입 방식 모두 일치. 모순 없음.
- **검증 항목 5** — 내부 참조 정확성: phase5.md에서 언급한 클래스명·패키지·파일 경로가 phase1~4 설계와 일치. `build/reports/jacoco/test/html/index.html` 리포트 경로는 Gradle JaCoCo 기본값과 일치.
- **결과**: 이상 없음 (WARNING-1 제외)
