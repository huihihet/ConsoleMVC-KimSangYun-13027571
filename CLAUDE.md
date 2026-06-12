# ConsoleMVC — CLAUDE.md

> 부모 규칙: `과제/CLAUDE.md` 상속. 이 파일은 ConsoleMVC PoC 전용 추가 규칙만 기술한다.

## 프로젝트 목적

MVC 아키텍처 패턴을 Java 콘솔 환경에서 패키지 구조로 검증하는 PoC.  
프레임워크 없이 순수 Java로 Model·Controller·View 역할 분리가 실제로 동작하는지 확인한다.  
이 PoC의 패턴은 SSemi 본 프로젝트의 MVC 구조 기반이 된다.

---

## 기술 스택

- **언어**: Java 17+
- **빌드**: Gradle 8.x
- **테스트**: JUnit Jupiter 6.x
- **런타임**: 콘솔 (표준 입출력)
- **외부 의존성**: 없음 (순수 Java)

---

## 패키지 구조

```
src/main/java/org/example/
├── Main.java                        # 진입점 — 객체 조립 및 루프 실행
├── model/
│   ├── entity/                      # 순수 도메인 객체 (POJO)
│   └── repository/                  # CRUD 인터페이스 + InMemory 구현체
├── controller/                      # 입력 수신 → 모델 호출 → 뷰 위임
├── view/                            # System.out 출력 및 포맷팅 전용
└── app/
    └── Router.java                  # 메뉴 라우팅

src/test/java/org/example/           # main과 미러링된 패키지 구조
```

---

## 역할 분리 규칙 (엄격 준수)

| 레이어 | 허용 | 금지 |
|--------|------|------|
| **Model** | 도메인 로직, 상태 관리, 유효성 검사 | View·Controller import, 콘솔 I/O |
| **Controller** | Model 호출, View 위임, 입력 파싱 | `System.out` 직접 출력, 도메인 로직 |
| **View** | `System.out` 출력, 포맷팅 | Model 직접 수정, 비즈니스 로직 |

---

## 코딩 컨벤션

- 클래스명: `PascalCase` / 메서드·변수명: `camelCase` / 상수: `UPPER_SNAKE_CASE`
- 패키지명: 전부 소문자
- Controller: 반드시 Constructor Injection으로 의존성 수신
- View: 인스턴스 메서드로 렌더링 — `static` 출력 메서드 금지
- 인터페이스: `I` 접두사 없이 의미 중심 명명 (e.g., `StudentRepository`)
- InMemory 구현체: `InMemory` 접두사 사용 (e.g., `InMemoryStudentRepository`)
- 주석: WHY가 비자명한 경우에만 한 줄 이내

---

## 테스트 전략

- **Model**: 비즈니스 로직, 경계값, 예외 케이스 단위 테스트
- **Controller**: Mock/Spy View 주입 후 흐름 검증
- **View**: `System.out` 캡처로 출력 포맷 검증
- **통합**: `Main` 흐름을 시나리오 기반으로 작성
- 목표 커버리지: Model·Controller 핵심 로직 **80% 이상**

---

## 빌드 & 실행

```bash
./gradlew build   # 빌드
./gradlew run     # 실행
./gradlew test    # 테스트
```

---

## PoC 범위 제한

- DB·파일 영속성, 네트워크, 인증, 멀티 스레딩은 **구현하지 않는다**
- 인메모리 저장소는 `List` 또는 `Map`으로 구현
- 설계 변경 필요 시 `docs/PRD.md` 먼저 수정 후 코드 변경
