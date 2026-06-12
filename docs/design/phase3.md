# Phase 3 설계 — Controller · View 구현

> 기준: `docs/PLAN.md > Phase 3` / PRD 마일스톤: M3  
> 목표: `SampleView`와 `SampleController`를 구현하고 단위 테스트를 작성한다.

---

## 1. 선행 작업 — `build.gradle` Mockito 추가

Phase 3 구현 시작 전 `build.gradle`의 `dependencies` 블록에 추가:

```groovy
testImplementation 'org.mockito:mockito-core:5.+'
testImplementation 'org.mockito:mockito-junit-jupiter:5.+'
```

---

## 2. `SampleView` 설계

출력 전용 클래스 — `System.out` 사용, 비즈니스 로직·조건 분기 없음.

### 필드

```java
private static final String SEPARATOR = "=".repeat(50);
```

### 메서드 목록

| 메서드 시그니처 | 출력 내용 |
|----------------|-----------|
| `printMenu()` | 메인 메뉴 번호 목록 |
| `printSampleList(List<Sample> samples)` | ID·이름·생산시간·수율·재고 표 형식 |
| `printSampleDetail(Sample sample)` | 단건 상세 정보 |
| `printSuccess(String message)` | `[성공] {message}` |
| `printError(String message)` | `[오류] {message}` |
| `printEmpty()` | `조회 결과가 없습니다.` |
| `printPrompt(String prompt)` | 줄바꿈 없이 입력 프롬프트 출력 (Controller의 System.out 직접 호출 금지 준수) |
| `printUpdateFieldMenu()` | 수정 필드 선택 메뉴 (`1.이름 2.생산시간 3.수율 4.재고`) |

### 출력 형식

```
// printMenu()
==============================
  S-Semi 시료 관리 시스템
==============================
1. 시료 등록
2. 시료 목록 조회
3. 시료 조회 (ID)
4. 시료 수정
5. 시료 삭제
6. 이름 검색
0. 종료
선택 > 

// printSampleList()
ID  | 이름            | 생산시간(min) | 수율  | 재고
----|-----------------|--------------|-------|-----
1   | AlphaChip       | 30           | 0.95  | 100

// printSampleDetail()
=== 시료 상세 ===
ID           : 1
이름         : AlphaChip
평균생산시간 : 30min
수율         : 0.95
재고         : 100개

// printSuccess()
[성공] 시료가 등록되었습니다.

// printError()
[오류] 해당 시료를 찾을 수 없습니다.

// printEmpty()
조회 결과가 없습니다.
```

### 규칙
- 모든 메서드는 `System.out.println` 사용
- `if`/`for` 등 로직 포함 불가 — 반복 출력은 `samples.forEach()` 스트림 사용
- `static` 메서드 금지 — 인스턴스 메서드로만 구현

---

## 3. `SampleController` 설계

### 생성자 — Constructor Injection

```java
public SampleController(SampleRepository repository, SampleView view, Scanner scanner) {
    this.repository = repository;
    this.view       = view;
    this.scanner    = scanner;
}
```

### 메서드 목록

| 메서드 | PRD | 동작 흐름 |
|--------|-----|-----------|
| `register()` | F-01 | 이름·생산시간·수율 입력 → `repository.save()` → `view.printSuccess()` |
| `listAll()` | F-02 | `repository.findAll()` → 비어있으면 `view.printEmpty()`, 아니면 `view.printSampleList()` |
| `findById()` | F-03 | ID 입력 → `repository.findById()` → 없으면 `view.printError()`, 있으면 `view.printSampleDetail()` |
| `update()` | F-04 | ID 입력 → 존재 확인 → 필드별 입력 → `repository.update()` → `view.printSuccess()` |
| `delete()` | F-05 | ID 입력 → `repository.deleteById()` → 결과에 따라 `view.printSuccess()` / `view.printError()` |
| `searchByName()` | F-06 | 키워드 입력 → `repository.findByNameContaining()` → `view.printSampleList()` / `view.printEmpty()` |
| `handleInvalidMenu()` | F-07 | `view.printError("유효하지 않은 메뉴입니다.")` |

### 입력 파싱 규칙

- `scanner.nextLine()` 사용 (nextInt 혼용 금지 — 개행 문자 잔류 문제 방지)
- Long 파싱 실패(`NumberFormatException`) → `view.printError()` 호출 후 메서드 종료
- int/double 파싱 실패 → 동일하게 `view.printError()` 후 종료
- `System.out` 직접 호출 금지 — 모든 출력은 `view` 위임

### update() 흐름 상세

```
1. ID 입력 → 파싱 실패 시 종료
2. repository.findById(id) → empty이면 view.printError("해당 시료를 찾을 수 없습니다.") 후 종료
3. 변경할 필드 선택 메뉴 출력 (1.이름 / 2.생산시간 / 3.수율 / 4.재고)
4. 선택한 필드 값 입력
5. 기존 Sample 객체의 update*() 메서드 호출
6. repository.update(sample) → view.printSuccess()
```

---

## 4. `SampleViewTest` 설계

`System.out`을 `ByteArrayOutputStream`으로 교체해 출력 문자열을 캡처한다.

### 픽스처

```java
@BeforeEach
void setUp() {
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
    view = new SampleView();
}

@AfterEach
void tearDown() {
    System.setOut(System.out);  // 원래 stdout 복원
}
```

### 테스트 케이스

| 케이스 | 검증 내용 |
|--------|-----------|
| `printMenu` | "시료 등록", "종료" 문자열 포함 여부 |
| `printSampleList` 단건 | ID, 이름, 생산시간, 수율, 재고 값 포함 여부 |
| `printSampleList` 빈 리스트 | 헤더만 출력되거나 내용 없음 |
| `printSampleDetail` | 모든 필드 값 포함 여부 |
| `printSuccess` | "[성공]" + 전달한 메시지 포함 |
| `printError` | "[오류]" + 전달한 메시지 포함 |
| `printEmpty` | "조회 결과가 없습니다." 포함 |

---

## 5. `SampleControllerTest` 설계

`InMemorySampleRepository`(실제) + Mockito Spy `SampleView` 조합으로 흐름을 검증한다.

### 픽스처

```java
@ExtendWith(MockitoExtension.class)
class SampleControllerTest {

    private SampleRepository     repository;
    @Spy SampleView              view;
    private SampleController     controller;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        repository = new InMemorySampleRepository();
        // Scanner는 테스트별로 입력 시나리오를 StringReader로 주입
    }
}
```

### 입력 주입 패턴

```java
// 각 테스트에서 Scanner 입력을 문자열로 시뮬레이션
Scanner scanner = new Scanner("AlphaChip\n30\n0.95\n");
controller = new SampleController(repository, view, scanner);
controller.register();
```

### 테스트 케이스

| 케이스 | 검증 내용 |
|--------|-----------|
| `register` 정상 | repository에 저장됨 + `view.printSuccess()` 1회 호출 |
| `register` 숫자 파싱 실패 | `view.printError()` 호출, repository 저장 없음 |
| `listAll` 빈 저장소 | `view.printEmpty()` 호출 |
| `listAll` 데이터 존재 | `view.printSampleList()` 호출 |
| `findById` 존재 ID | `view.printSampleDetail()` 호출 |
| `findById` 미존재 ID | `view.printError()` 호출 |
| `findById` ID 파싱 실패 | `view.printError()` 호출 |
| `update` 정상 | repository에 변경 반영 + `view.printSuccess()` 호출 |
| `update` 미존재 ID | `view.printError()` 호출, repository 변경 없음 |
| `delete` 존재 ID | repository에서 삭제 + `view.printSuccess()` 호출 |
| `delete` 미존재 ID | `view.printError()` 호출 |
| `searchByName` 결과 있음 | `view.printSampleList()` 호출 |
| `searchByName` 결과 없음 | `view.printEmpty()` 호출 |
| `handleInvalidMenu` | `view.printError("유효하지 않은 메뉴입니다.")` 호출 |

---

## 6. 완료 기준

- [ ] `build.gradle`에 Mockito 5.x 의존성 추가
- [ ] `SampleView` — `System.out` 전용, 비즈니스 로직 없음
- [ ] `SampleController` — `System.out` 직접 호출 없음, F-01~F-07 전 기능 구현
- [ ] `SampleViewTest` 전 케이스 통과
- [ ] `SampleControllerTest` 전 케이스 통과
- [ ] `./gradlew test` BUILD SUCCESSFUL
