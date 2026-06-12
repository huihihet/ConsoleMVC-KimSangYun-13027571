# Phase 4 설계 — 조립 및 통합 시나리오 검증

> 기준: `docs/PLAN.md > Phase 4` / PRD 마일스톤: M4  
> 목표: `Router`·`Main`에서 객체를 조립하고, 전체 흐름을 통합 시나리오로 검증한다.

---

## 1. `Router` 설계

### 역할
- 메뉴 번호 → `SampleController` 메서드 라우팅
- `SampleController`만 보유 — **View 직접 참조 금지** (PLAN.md Warning-2 반영)
- 잘못된 메뉴 번호 → `controller.handleInvalidMenu()` 위임

### 구현

```java
package org.example.app;

import org.example.controller.SampleController;

public class Router {

    private final SampleController controller;

    public Router(SampleController controller) {
        this.controller = controller;
    }

    // true: 루프 계속, false: 종료(메뉴 0)
    public boolean route(int menu) {
        switch (menu) {
            case 1 -> controller.register();
            case 2 -> controller.listAll();
            case 3 -> controller.findById();
            case 4 -> controller.update();
            case 5 -> controller.delete();
            case 6 -> controller.searchByName();
            case 0 -> { return false; }
            default -> controller.handleInvalidMenu();
        }
        return true;
    }
}
```

---

## 2. `Main` 설계

### 역할
- 모든 의존성을 조립하는 진입점
- 메뉴 입력 루프 실행 (F-07)
- 숫자가 아닌 입력 → 오류 메시지 출력 후 재입력

### 구현

```java
package org.example;

import org.example.app.Router;
import org.example.controller.SampleController;
import org.example.model.repository.InMemorySampleRepository;
import org.example.model.repository.SampleRepository;
import org.example.view.SampleView;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner          scanner    = new Scanner(System.in);
        SampleRepository repository = new InMemorySampleRepository();
        SampleView       view       = new SampleView();
        SampleController controller = new SampleController(repository, view, scanner);
        Router           router     = new Router(controller);

        while (true) {
            view.printMenu();
            try {
                int menu = Integer.parseInt(scanner.nextLine().trim());
                if (!router.route(menu)) break;
            } catch (NumberFormatException e) {
                view.printError("메뉴는 숫자로 입력하세요.");
            }
        }
    }
}
```

---

## 3. `SampleFlowIntegrationTest` 설계

### 목적
전체 레이어(Model → Repository → Controller → View)가 함께 동작하는지 시나리오 기반으로 검증한다.  
`Router`를 직접 호출하거나 `SampleController` 메서드를 순서대로 호출해 실제 흐름을 재현한다.

### 픽스처

```java
@BeforeEach
void setUp() {
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));

    repository = new InMemorySampleRepository();
    view       = new SampleView();
}

@AfterEach
void tearDown() {
    System.setOut(originalOut);
}

private SampleController controller(String input) {
    return new SampleController(repository, view, new Scanner(input));
}

private String output() {
    return outContent.toString(StandardCharsets.UTF_8);
}
// 시나리오 간 출력 혼재 방지가 필요한 시점에 outContent.reset() 호출
```

### 시나리오별 테스트 케이스

| 시나리오 | 입력 흐름 | 검증 내용 |
|----------|-----------|-----------|
| 등록 → 목록 조회 | `register("AlphaChip\n30\n0.95\n")` → `listAll()` | 목록 출력에 "AlphaChip" 포함 |
| 등록 → 수정 → 단건 조회 | `register` → `update("1\n1\nBetaChip\n")` → `findById("1\n")` | 상세 출력에 "BetaChip" 포함 |
| 등록 → 삭제 → 단건 조회 | `register` → `delete("1\n")` → `findById("1\n")` | `[오류]` 포함 |
| 복수 등록 → 이름 검색 | `register`×2 → `searchByName("Alpha\n")` | "AlphaChip"만 포함, "BetaChip" 미포함 |
| 잘못된 메뉴 번호 | `Router.route(9)` | `[오류]` + "유효하지 않은 메뉴입니다." 포함 |
| 메뉴 0 입력 시 종료 | `Router.route(0)` | `false` 반환 |

> 각 시나리오에서 `outContent`를 테스트 간 공유하므로, 호출 전후 `outContent.reset()`으로 출력 버퍼를 초기화해 이전 출력과 혼재되지 않도록 한다.

---

## 4. `RouterTest` 설계 (선택)

`Router`가 올바른 Controller 메서드를 호출하는지 단위 검증한다.

```java
@ExtendWith(MockitoExtension.class)
class RouterTest {

    @Mock SampleController controller;
    Router router;

    @BeforeEach
    void setUp() { router = new Router(controller); }
}
```

| 케이스 | 검증 내용 |
|--------|-----------|
| `route(1)` | `controller.register()` 1회 호출, `true` 반환 |
| `route(2)` | `controller.listAll()` 1회 호출, `true` 반환 |
| `route(3)` | `controller.findById()` 1회 호출, `true` 반환 |
| `route(4)` | `controller.update()` 1회 호출, `true` 반환 |
| `route(5)` | `controller.delete()` 1회 호출, `true` 반환 |
| `route(6)` | `controller.searchByName()` 1회 호출, `true` 반환 |
| `route(0)` | 어떤 메서드도 호출 안 됨, `false` 반환 |
| `route(9)` | `controller.handleInvalidMenu()` 1회 호출, `true` 반환 |

---

## 5. 파일 목록

| 파일 | 작업 |
|------|------|
| `src/main/java/org/example/app/Router.java` | 신규 구현 |
| `src/main/java/org/example/Main.java` | 신규 구현 |
| `src/test/java/org/example/integration/SampleFlowIntegrationTest.java` | placeholder 교체 |
| `src/test/java/org/example/app/RouterTest.java` | 신규 생성 |

---

## 6. 완료 기준

- [ ] `Router.route()` — 메뉴 1~6 정상 라우팅, 0 종료, 그 외 오류 처리
- [ ] `Main` — 의존성 조립 완료, `./gradlew run` 실행 후 F-01~F-07 전 기능 동작
- [ ] 통합 시나리오 테스트 6개 전부 통과
- [ ] `RouterTest` 8개 케이스 통과
- [ ] `Main` — `NoSuchElementException`(stdin EOF) 발생 시 루프 정상 종료
- [ ] Main 루프 통합 테스트: 숫자 아닌 입력 재시도, 정상 등록 후 0 종료
- [ ] `./gradlew test` BUILD SUCCESSFUL
