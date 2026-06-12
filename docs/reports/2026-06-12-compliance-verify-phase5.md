# 컴플라이언스 검증 보고서

**일시**: 2026-06-12
**검증 대상**: `docs/design/phase5.md` — Phase 5 최종 검증
**결과**: ❌ 위반 3건 (CRITICAL: 1, WARNING: 2)

---

## 발견된 위반

### [CRITICAL] jacocoTestCoverageVerification includes 패턴 오류 — Controller 커버리지 강제 검증 무력화

- **위치**: `docs/design/phase5.md` — §4 JaCoCo 최소 커버리지 강제 코드 블록
- **위반 규칙**: 기술 스택 규칙 정확성 (ConsoleMVC CLAUDE.md 기술 스택, Gradle 8.x 빌드 도구 정확한 사용)
- **현재 설계**:
  ```groovy
  includes = ['org.example.model.*', 'org.example.controller']
  ```
  `'org.example.controller'`에 와일드카드(`.*`)가 없다. JaCoCo `jacocoTestCoverageVerification`의 `includes` 패턴은 완전한 클래스명(FQCN) 수준 glob으로 동작한다. `org.example.controller`는 패키지명 자체이므로 `SampleController` 클래스와 매칭되지 않아 Controller 커버리지 강제 검증이 실질적으로 비활성화된다. PRD 수용 기준 §8 "Model·Controller 커버리지 80% 이상"을 빌드 레벨에서 보장하려는 설계 의도가 무력화된다.
- **권장 수정**:
  ```groovy
  includes = ['org.example.model.*', 'org.example.controller.*']
  ```

---

### [WARNING] check.dependsOn이 "선택 사항" 코드 블록 안에 혼입 — 구현 시 의도치 않은 빌드 실패 위험

- **위치**: `docs/design/phase5.md` — §4 JaCoCo 최소 커버리지 강제 코드 블록 마지막 줄
- **위반 규칙**: ConsoleMVC CLAUDE.md [불필요한 복잡성] — 요구 사항에 없는 동작이 설계에 포함될 위험
- **현재 설계**: 본문에서 "Phase 5에서는 리포트 확인 후 수치를 파악하는 것이 1차 목표이며, 강제 적용은 선택 사항이다"라고 명시했음에도 제시된 코드 블록 안에 `check.dependsOn jacocoTestCoverageVerification`이 포함되어 있다. 구현자가 블록을 그대로 적용하면 `./gradlew build` 실행 시 커버리지 미달로 빌드가 실패한다. PRD 수용 기준 §1 "`./gradlew build`가 경고 없이 성공"과 충돌한다.
- **권장 수정**: `check.dependsOn jacocoTestCoverageVerification` 라인을 코드 블록에서 분리하고, "선택 적용 시 별도로 추가"라는 안내 문구와 함께 독립 코드 스니펫으로 제시하거나 주석 처리(`// check.dependsOn jacocoTestCoverageVerification`)로 표기한다.

---

### [WARNING] §2-2 Model grep 패턴이 PLAN.md와 불일치 — System.err 검사 누락

- **위치**: `docs/design/phase5.md` — §2-2 Model grep 검증 명령
- **위반 규칙**: PLAN.md Phase 5 체크리스트 "Model 클래스에 `Scanner` 또는 `System` 코드 없음 (grep 확인)"과의 정합성
- **현재 설계**:
  ```bash
  grep -rn "Scanner\|System\.in\|System\.out" src/main/java/org/example/model/
  ```
  PLAN.md는 `System` 전체를 검사 대상으로 명시했으나, phase5.md의 grep 패턴은 `System.in`과 `System.out`만 검사하고 `System.err`를 포함하지 않는다. Model 레이어에서 `System.err.println()` 등을 사용해도 이 grep 명령은 탐지하지 못한다.
- **권장 수정**:
  ```bash
  grep -rn "Scanner\|System\." src/main/java/org/example/model/
  ```

---

## 검증 결과 요약

- [A] 아키텍처 제약: ✅ (패키지 구조 규칙 준수, 레이어 분리 위반 없음, 수정 금지 파일 미건드림)
- [B] 코딩 컨벤션: ✅ (phase5.md는 신규 소스 파일을 생성하지 않으며, build.gradle 수정만 포함 — 명명 규칙 적용 대상 없음)
- [C] 보안: ✅ (PoC 범위 제한 위반 없음, DB·파일 영속성·네트워크 내용 없음, 민감 정보 처리 없음)
- [D] 불필요한 복잡성: ❌ (CRITICAL 1건: includes 패턴 오류로 검증 무력화 / WARNING 1건: 선택 사항 코드에 check.dependsOn 혼입)
