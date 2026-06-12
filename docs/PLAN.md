# PLAN — ConsoleMVC PoC

> 기준 문서: `docs/PRD.md`  
> 개발 순서: 스켈레톤 → Model → Controller·View → 조립 → 검증

---

## Phase 1. 프로젝트 스켈레톤 `> PRD 마일스톤: M1`

**목표**: 패키지 구조와 빈 클래스를 생성하고 빌드가 통과하는 상태를 만든다.

### 작업 목록

1. Gradle 프로젝트 초기화 (`build.gradle`, `settings.gradle`)
   - Java 17, JUnit Jupiter 6.x 의존성 설정
   - `application` 플러그인 + mainClass 지정
2. 패키지 디렉터리 생성
   ```
   src/main/java/org/example/
   ├── Main.java
   ├── model/entity/
   ├── model/repository/
   ├── controller/
   ├── view/
   └── app/
   src/test/java/org/example/   (미러링)
   ```
3. 빈 클래스 파일 생성 (컴파일 가능한 상태)
   - `Sample.java`, `SampleRepository.java`, `InMemorySampleRepository.java`
   - `SampleController.java`, `SampleView.java`
   - `Router.java`, `Main.java`

### 완료 기준

- [ ] `./gradlew build` 성공
- [ ] 모든 클래스 파일이 패키지 규칙에 맞게 위치

---

## Phase 2. Model 구현 `> PRD 마일스톤: M2`

**목표**: 도메인 엔티티와 인메모리 저장소를 구현하고 단위 테스트를 작성한다.

### 2-1. `Sample` 엔티티

```java
// model/entity/Sample.java
class Sample {
    Long   sampleId          // 자동 증가
    String name              // 공백 불허
    int    avgProductionTime // 1 이상
    double yield             // 0.0 초과 ~ 1.0 이하
    int    stock             // 0 이상
}
```

- 생성자에서 유효성 검증 — 위반 시 `IllegalArgumentException`
- `updateName()`, `updateAvgProductionTime()`, `updateYield()`, `updateStock()` 수정 메서드 제공
- `toString()` 오버라이드 (조회 출력용)

### 2-2. `SampleRepository` 인터페이스

```java
interface SampleRepository {
    Sample save(Sample sample);
    List<Sample> findAll();
    Optional<Sample> findById(Long id);
    List<Sample> findByNameContaining(String keyword);
    boolean update(Sample sample);
    boolean deleteById(Long id);
}
```

### 2-3. `InMemorySampleRepository` 구현체

- `ArrayList<Sample>` 기반 저장
- `sampleId` 자동 증가 (AtomicLong 또는 필드 카운터)
- `findByNameContaining`: `name.contains(keyword)` 필터링

### 2-4. 단위 테스트 (`SampleTest`, `InMemorySampleRepositoryTest`)

**테스트 픽스처**: 각 테스트 클래스에 `@BeforeEach`로 공통 `Sample` 객체를 초기화한다.
`InMemorySampleRepository`는 매 테스트마다 새 인스턴스를 생성해 테스트 간 상태 오염을 방지한다.

**placeholder 처리**: Phase 1의 `placeholder()` 메서드는 Phase 2 시작 시 삭제하고 아래 케이스들로 교체한다.

**Phase 1 → Phase 2 전환 절차**: `SampleRepository` 인터페이스에 메서드를 추가하는 순간
`InMemorySampleRepository`의 컴파일이 실패한다. 다음 순서로 진행한다:
1. `SampleRepository` 인터페이스에 메서드 시그니처 추가
2. 즉시 `InMemorySampleRepository`에 빈 구현(`return null` / `return false`) 추가 → 컴파일 통과
3. 구현 내용을 채운 뒤 테스트 작성

| 테스트 케이스 | 검증 내용 |
|--------------|-----------|
| 정상 생성 | 모든 필드 저장 확인 |
| 수율 범위 위반 (`yield < 0.0`, `yield > 1.0`) | `IllegalArgumentException` 발생 |
| 수율 하한 경계값 (`yield = 0.0`) | `IllegalArgumentException` 발생 (0.0 초과 조건) |
| 수율 상한 경계값 (`yield = 1.0`) | 정상 생성 |
| 생산 시간 0 이하 | `IllegalArgumentException` 발생 |
| 재고 음수 | `IllegalArgumentException` 발생 |
| 이름 공백·빈 문자열 | `IllegalArgumentException` 발생 |
| save → findById | 저장 후 조회 일치 |
| findAll | 전체 목록 반환 |
| findByNameContaining (키워드 포함) | 해당 시료만 반환 |
| findByNameContaining (빈 문자열 `""`) | 전체 시료 반환 |
| update | 수정 후 findById로 변경 확인 |
| deleteById (존재) | `true` 반환 + 조회 불가 |
| deleteById (미존재) | `false` 반환 |

### 완료 기준

- [ ] `Sample` 유효성 검증 동작
- [ ] `InMemorySampleRepository` CRUD 정상 동작
- [ ] 단위 테스트 전부 통과, 커버리지 80% 이상

---

## Phase 3. Controller · View 구현 `> PRD 마일스톤: M3`

**목표**: 사용자 입력을 파싱하고 Model을 호출한 뒤 View에 결과를 위임하는 레이어를 구현한다.

### 3-0. `build.gradle` 의존성 추가 (Phase 3 시작 시 선행)

`SampleControllerTest`에서 `SampleView`를 Mock/Spy로 주입하려면 Mockito가 필요하다.
Phase 3 구현 전에 `build.gradle`의 `dependencies` 블록에 아래를 추가한다:

```groovy
testImplementation 'org.mockito:mockito-core:5.+'
testImplementation 'org.mockito:mockito-junit-jupiter:5.+'
```

### 3-1. `SampleView`

출력 전용 — `System.out` 사용, 비즈니스 로직 없음

| 메서드 | 출력 내용 |
|--------|-----------|
| `printMenu()` | 메인 메뉴 번호 목록 |
| `printSampleList(List<Sample>)` | ID·이름·생산시간·수율·재고 표 형식 |
| `printSampleDetail(Sample)` | 단건 상세 정보 |
| `printSuccess(String)` | 성공 메시지 |
| `printError(String)` | 오류 메시지 |
| `printEmpty()` | 조회 결과 없음 메시지 |

### 3-2. `SampleController`

Constructor Injection — `SampleRepository`, `SampleView`, `Scanner` 주입

| 메서드 | 담당 기능 |
|--------|-----------|
| `register()` | F-01 시료 등록 |
| `listAll()` | F-02 전체 목록 조회 |
| `findById()` | F-03 ID 단건 조회 |
| `update()` | F-04 시료 수정 |
| `delete()` | F-05 시료 삭제 |
| `searchByName()` | F-06 이름 검색 |
| `handleInvalidMenu()` | F-07 잘못된 메뉴 번호 오류 출력 (Router에서 위임) |

- `System.out` 직접 호출 금지 — 모든 출력은 `SampleView` 위임
- 입력 파싱 실패 시 `SampleView.printError()` 호출 후 메서드 종료

### 3-3. 단위 테스트

- `SampleViewTest`: `System.out` 캡처(`ByteArrayOutputStream`)로 출력 포맷 검증
- `SampleControllerTest`: `InMemorySampleRepository` + Spy/Mock `SampleView` 주입, 각 흐름 검증

### 완료 기준

- [ ] Controller에 `System.out` 직접 호출 없음
- [ ] View에 조건 분기·계산 로직 없음
- [ ] Controller 단위 테스트 전부 통과

---

## Phase 4. 조립 및 통합 시나리오 검증 `> PRD 마일스톤: M4`

**목표**: `Main`·`Router`에서 객체를 조립하고, 전체 흐름을 시나리오로 검증한다.

### 4-1. `Router` (F-07 메뉴 네비게이션 구현)

PRD F-07의 메뉴 번호 선택 및 `0` 입력 시 종료 흐름은 `Router`와 `Main` 루프에서 구현한다.  
잘못된 메뉴 번호 입력 시 오류 메시지 출력은 `SampleController.handleInvalidMenu()`에 위임한다 — Router가 View를 직접 보유하지 않는다.

```java
class Router {
    // SampleController만 보유 (View 직접 참조 금지)
    boolean route(int menu);  // true: 계속, false: 종료(0 입력)
                              // 유효 번호 → Controller 메서드 호출
                              // 유효하지 않은 번호 → controller.handleInvalidMenu() 위임
}
```

`SampleController`에 추가:
```java
void handleInvalidMenu();  // view.printError("유효하지 않은 메뉴입니다.") 호출
```

### 4-2. `Main`

```java
// 의존성 조립
SampleRepository repo       = new InMemorySampleRepository();
SampleView       view       = new SampleView();
SampleController controller = new SampleController(repo, view, scanner);
Router           router     = new Router(controller);  // View 미주입

// 메인 루프 (F-07)
while (true) {
    view.printMenu();
    int menu = scanner.nextInt();
    if (!router.route(menu)) break;
}
```

### 4-3. 통합 시나리오 테스트

| 시나리오 | 입력 흐름 | 검증 내용 |
|----------|-----------|-----------|
| 시료 등록 후 목록 조회 | F-01 → F-02 | 등록한 시료가 목록에 표시 |
| 수정 후 단건 조회 | F-01 → F-04 → F-03 | 수정된 값 반영 확인 |
| 삭제 후 조회 | F-01 → F-05 → F-03 | 오류 메시지 출력 확인 |
| 이름 검색 | F-01(복수) → F-06 | 키워드 포함 시료만 출력 |
| 잘못된 메뉴 번호 | 숫자 범위 밖 입력 | 오류 메시지 후 재입력 |

### 완료 기준

- [ ] `./gradlew run` 실행 후 F-01~F-07 전 기능 동작
- [ ] 통합 시나리오 테스트 전부 통과

---

## Phase 5. 최종 검증 `> PRD 마일스톤: M5`

**목표**: 역할 분리 규칙 준수 여부를 확인하고 커버리지를 측정한다.

### 체크리스트

- [ ] Controller 클래스에 `System.out` 직접 호출 없음 (`grep` 확인)
- [ ] Model 클래스에 `Scanner` 또는 `System` 코드 없음 (`grep` 확인)
- [ ] View 클래스에 `if`/`for` 비즈니스 로직 없음 (코드 리뷰)
- [ ] `./gradlew test` 전 테스트 통과
- [ ] `./gradlew jacocoTestReport` — Model·Controller 커버리지 80% 이상
- [ ] PRD 수용 기준(Acceptance Criteria) 전 항목 충족

---

## 파일 생성 체크리스트 (전체)

```
src/main/java/org/example/
├── Main.java
├── model/
│   ├── entity/
│   │   └── Sample.java
│   └── repository/
│       ├── SampleRepository.java
│       └── InMemorySampleRepository.java
├── controller/
│   └── SampleController.java
├── view/
│   └── SampleView.java
└── app/
    └── Router.java

src/test/java/org/example/
├── model/
│   ├── entity/
│   │   └── SampleTest.java
│   └── repository/
│       └── InMemorySampleRepositoryTest.java
├── controller/
│   └── SampleControllerTest.java
├── view/
│   └── SampleViewTest.java
└── integration/
    └── SampleFlowIntegrationTest.java
```
