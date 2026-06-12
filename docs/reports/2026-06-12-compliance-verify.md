# 컴플라이언스 검증 보고서

**일시**: 2026-06-12  
**검증 대상**: `docs/design/phase1.md` — Phase 1 스켈레톤 구현  
**결과**: ❌ 위반 2건 (CRITICAL: 0, WARNING: 2)

---

## 발견된 위반

### [WARNING] build.gradle에 설계 문서에 없는 커스텀 태스크 추가
- **위치**: `build.gradle` — 28~48번 줄
- **위반 규칙**: CLAUDE.md [불필요한 복잡성] — "요구 사항에 없는 기능이 설계에 포함됐는지 (오버엔지니어링)"
- **현재 구현**: `phase1.md` 섹션 2의 `build.gradle` 설계에는 `test { useJUnitPlatform() }` 한 블록만 명시되어 있다. 그러나 실제 구현에는 아래 내용이 추가되었다.
  - `copyTestClassesToAsciiPath` 커스텀 Copy 태스크
  - `copyMainClassesToAsciiPath` 커스텀 Copy 태스크
  - `test` 블록 재정의 (`classpath` 교체 포함)
- **판단 근거**: WHY 주석(`// Windows CP949 환경에서 Gradle Worker @args 파일 내 한글 경로가 깨지는 문제 우회`)이 작성되어 있어 추가 이유는 명확하다. 그러나 이 우회 코드는 설계 단계에서 검토·승인된 내용이 아니며, 구현 단계에서 설계 범위를 벗어나 추가되었다.
- **권장 수정**: `phase1.md`의 `build.gradle` 설계 섹션에 해당 우회 필요성과 태스크 구조를 명시하거나, 또는 `gradle.properties`의 JVM 인코딩 설정만으로 문제가 해결되는지 확인 후 커스텀 태스크를 제거한다. 설계 문서 선행 수정(`docs/design/phase1.md` 또는 `docs/PRD.md`) 후 구현이 원칙이다.

---

### [WARNING] gradle.properties 파일이 설계 문서에 정의되지 않은 채 추가됨
- **위치**: `gradle.properties` — 프로젝트 루트
- **위반 규칙**: CLAUDE.md 문서 관리 규칙 — "기능 추가·변경 전에 반드시 `docs/PRD.md`를 먼저 수정한다" / `phase1.md` 섹션 2의 파일 목록에 `gradle.properties` 미포함
- **현재 구현**: `org.gradle.jvmargs=-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8` — 인코딩 관련 JVM 인수 설정
- **판단 근거**: 파일 자체의 내용은 보안·아키텍처 위반이 없고, 환경 문제 대응 목적이 명확하다. 그러나 설계 문서에 포함되지 않은 파일이 구현 단계에서 추가되었다는 점에서 문서 선행 수정 규칙을 위반한다.
- **권장 수정**: `phase1.md` 또는 `docs/PRD.md`에 `gradle.properties` 추가 필요성(Windows 한글 경로 인코딩 문제)을 명시하고, 이후 구현을 확정한다.

---

## 검증 결과 요약

| 항목 | 결과 | 세부 사항 |
|------|------|-----------|
| [A] 아키텍처 제약 | ✅ | 패키지 구조, 레이어 분리, 금지된 의존 방향 모두 준수 |
| [B] 코딩 컨벤션 | ✅ | PascalCase, 패키지 소문자, InMemory 접두사, I 접두사 없는 인터페이스명 모두 준수. 주석은 설계 문서 지정 패턴 그대로 사용 |
| [C] 보안 | ✅ | 스켈레톤 단계로 입력 처리 없음. 민감 정보 평문 저장 없음 |
| [D] 불필요한 복잡성 | ❌ | 설계 범위 외 커스텀 태스크 및 gradle.properties 추가 (WARNING 2건) |

---

## 참고: 준수 확인 항목

- 패키지명 전부 소문자: `org.example.model.entity`, `org.example.model.repository`, `org.example.controller`, `org.example.view`, `org.example.app` — 모두 준수
- 클래스명 PascalCase: `Sample`, `SampleRepository`, `InMemorySampleRepository`, `SampleController`, `SampleView`, `Router`, `Main` — 모두 준수
- 인터페이스 `I` 접두사 금지: `SampleRepository` (I 없음) — 준수
- InMemory 접두사: `InMemorySampleRepository` — 준수
- 역할 분리 위반 import 없음: Controller·View·app 레이어에 금지된 import 없음 — 준수
- 외부 의존성: JUnit 외 추가 없음 (`junit-bom:6.0.0`, `junit-jupiter`, `junit-platform-launcher`만 선언) — 준수
- Java 17, application 플러그인, mainClass 설정: 설계와 일치 — 준수
