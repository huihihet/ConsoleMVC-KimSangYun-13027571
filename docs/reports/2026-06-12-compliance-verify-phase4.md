# 컴플라이언스 검증 보고서

**일시**: 2026-06-12  
**검증 대상**: `docs/design/phase4.md` (Phase 4 — 조립 및 통합 시나리오 검증)  
**결과**: ❌ 위반 2건 (CRITICAL: 0, WARNING: 2)

---

## 발견된 위반

### [WARNING] Router.java — WHAT성 주석 작성
- **위치**: `src/main/java/org/example/app/Router.java` — 13번째 줄
- **위반 규칙**: CLAUDE.md 코딩 컨벤션 "주석: WHY가 비자명한 경우에만 한 줄 이내"
- **현재 설계**: `// true: 루프 계속, false: 종료(메뉴 0)` — 반환값의 동작(WHAT)을 설명하는 주석. `public boolean route(int menu)` 시그니처와 `case 0 -> { return false; }` 코드를 읽으면 자명하게 파악 가능한 내용이다.
- **권장 수정**: 해당 주석 제거. 반환값의 의미가 불명확하다고 판단된다면 메서드명을 `routeOrExit` 등으로 변경하거나 Javadoc(`@return`)으로 대체를 검토한다.

### [WARNING] SampleFlowIntegrationTest.java — 테스트 패키지 미러링 규칙 위배
- **위치**: `src/test/java/org/example/integration/SampleFlowIntegrationTest.java` — 패키지 선언 `package org.example.integration;`
- **위반 규칙**: CLAUDE.md 패키지 구조 "src/test/java/org/example/ — main과 미러링된 패키지 구조"
- **현재 설계**: 테스트 파일이 `org.example.integration` 패키지에 위치하지만, main 소스에는 대응하는 `org.example.integration` 패키지가 존재하지 않는다.
- **권장 수정**: 통합 테스트 파일을 검증 대상 주 클래스(`Router` 또는 `Main`)의 패키지를 기준으로 배치한다. `Router`의 흐름을 통합 검증하는 목적이라면 `org.example.app` 패키지로 이동하는 것이 미러링 규칙에 부합한다. 또는 CLAUDE.md에 통합 테스트 전용 패키지(`integration/`) 예외를 명시적으로 추가하여 규칙을 보완할 수 있다.

---

## 검증 결과 요약

| 항목 | 결과 | 비고 |
|------|------|------|
| [A] 아키텍처 제약 | ✅ | Router — View 직접 참조 없음. 의존 방향 역전 없음. 패키지 구조 준수. Constructor Injection 준수. |
| [B] 코딩 컨벤션 | ❌ | Router.java WHAT성 주석 (WARNING 1건). 네이밍 PascalCase/camelCase/UPPER_SNAKE_CASE 모두 준수. |
| [C] 보안 | ✅ | 경계에서만 입력 검증. OWASP 위험 없음. 민감 정보 평문 저장 없음. |
| [D] 불필요한 복잡성 | ✅ | 오버엔지니어링 없음. 요구사항 외 기능 없음. |
| [E] 테스트 패키지 구조 | ❌ | SampleFlowIntegrationTest 패키지가 미러링 규칙 미준수 (WARNING 1건). RouterTest는 정확히 미러링. |
