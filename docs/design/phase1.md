# Phase 1 설계 — 프로젝트 스켈레톤

> 기준: `docs/PLAN.md > Phase 1` / PRD 마일스톤: M1  
> 목표: 패키지 구조와 빈 클래스를 생성하고 `./gradlew build`가 통과하는 상태를 만든다.

---

## 1. 현재 상태

| 항목 | 상태 |
|------|------|
| Gradle 프로젝트 | 완료 (`build.gradle`, `settings.gradle`, Gradle 래퍼 존재) |
| Java 17 설정 | 미설정 — `build.gradle`에 `sourceCompatibility` 없음 |
| `application` 플러그인 | 미설정 — `mainClass` 미지정으로 `./gradlew run` 불가 |
| JUnit 6.x 의존성 | 완료 (`junit-bom:6.0.0`) |
| 패키지 디렉터리 | 미생성 — `src/main/java`, `src/test/java` 하위 비어 있음 |
| 빈 클래스 파일 | 미생성 |

---

## 2. `build.gradle` 수정 설계

```groovy
plugins {
    id 'java'
    id 'application'          // gradlew run 지원
}

group   = 'org.example'
version = '1.0-SNAPSHOT'

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass = 'org.example.Main'
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation platform('org.junit:junit-bom:6.0.0')
    testImplementation 'org.junit.jupiter:junit-jupiter'
    testRuntimeOnly    'org.junit.platform:junit-platform-launcher'
}

test {
    useJUnitPlatform()
}
```

### 실제 구현 시 추가된 항목 (Windows 한글 경로 우회)

Windows 환경에서 프로젝트 경로에 한글(`과제/`)이 포함되어 있어, Gradle 9.x + Java 26 조합에서
`@args` 파일의 UTF-8 classpath가 시스템 코드 페이지(CP949)로 잘못 파싱되는 문제가 발생한다.

**해결책**: `build.gradle`에 `copyTestClassesToAsciiPath` / `copyMainClassesToAsciiPath` 커스텀 Copy
태스크를 추가해 컴파일된 클래스를 ASCII 경로(`java.io.tmpdir` 하위)로 복사 후 해당 경로를 classpath로 사용.

**`gradle.properties` 신규 생성**: Gradle daemon JVM에 `-Dfile.encoding=UTF-8` 설정 추가 (보조 조치).

---

## 3. 패키지 구조 설계

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
└── integration/                          ← main 미러링 예외 패키지
    └── SampleFlowIntegrationTest.java
```

> **`integration` 패키지 예외 사유**: 통합 테스트는 특정 main 레이어에 종속되지 않고 전체 흐름을
> 검증하므로 main 미러링 구조에서 의도적으로 제외한다. 단일 예외이며 그 외 모든 테스트 패키지는
> main 구조를 미러링한다.

---

## 4. 빈 클래스 설계

각 클래스는 컴파일 가능한 최소 상태로 생성한다. 필드·메서드는 Phase 2~3에서 채운다.

### `Main.java`
```java
package org.example;

public class Main {
    public static void main(String[] args) {
        // Phase 4에서 구현
    }
}
```

### `model/entity/Sample.java`
```java
package org.example.model.entity;

public class Sample {
    // Phase 2에서 구현
}
```

### `model/repository/SampleRepository.java`
```java
package org.example.model.repository;

import org.example.model.entity.Sample;
import java.util.List;
import java.util.Optional;

public interface SampleRepository {
    // Phase 2에서 구현
}
```

### `model/repository/InMemorySampleRepository.java`
```java
package org.example.model.repository;

public class InMemorySampleRepository implements SampleRepository {
    // Phase 2에서 구현
}
```

### `controller/SampleController.java`
```java
package org.example.controller;

public class SampleController {
    // Phase 3에서 구현
}
```

### `view/SampleView.java`
```java
package org.example.view;

public class SampleView {
    // Phase 3에서 구현
}
```

### `app/Router.java`
```java
package org.example.app;

public class Router {
    // Phase 4에서 구현
}
```

---

## 5. 테스트 빈 클래스 설계

빌드 시 컴파일 오류 없이 통과할 수 있도록 최소 구조만 작성한다.

```java
// 공통 패턴 — 각 테스트 클래스 동일 구조
package org.example.{패키지};

import org.junit.jupiter.api.Test;

class {ClassName}Test {
    @Test
    void placeholder() {
        // Phase {N}에서 구현
    }
}
```

---

## 6. 완료 기준

- [ ] `./gradlew build` 경고 없이 성공
- [ ] `./gradlew run` 실행 시 Main 진입점 호출 확인
- [ ] `./gradlew test` 통과 (빈 테스트 포함)
- [ ] 모든 클래스가 CLAUDE.md 패키지 규칙에 맞게 위치
- [ ] `.idea/` 등 IDE 파일이 `.gitignore`에 포함되어 추적되지 않음
