# Phase 5 설계 — 최종 검증

> 기준: `docs/PLAN.md > Phase 5` / PRD 마일스톤: M5  
> 목표: 역할 분리 규칙 준수 여부를 grep으로 확인하고, JaCoCo 커버리지를 측정해 PRD 수용 기준 전 항목을 충족한다.

---

## 1. `build.gradle` JaCoCo 설정 추가

### 배경

현재 `build.gradle`은 Windows CP949 경로 문제 우회를 위해 컴파일된 클래스를  
ASCII 경로(`sysTmp/consolemvc-classes/`)로 복사한 뒤 `test` 태스크의 classpath를 교체한다.  
JaCoCo 에이전트는 JVM이 클래스를 로드할 때 인스트루먼트하므로  
실제로 로드되는 ASCII 경로 클래스를 분석 대상으로 지정해야 리포트가 생성된다.

### 변경 내용

```groovy
plugins {
    id 'java'
    id 'application'
    id 'jacoco'          // 추가
}

// ... 기존 내용 유지 ...

jacoco {
    toolVersion = "0.8.12"
}

test {
    dependsOn copyTestClassesToAsciiPath, copyMainClassesToAsciiPath
    useJUnitPlatform()
    classpath = files(asciiTestClasses, asciiMainClasses) +
                configurations.testRuntimeClasspath
    finalizedBy jacocoTestReport   // 추가: test 완료 후 리포트 자동 생성
}

jacocoTestReport {
    dependsOn test
    // ASCII 경로에 복사된 main 클래스를 분석 대상으로 지정
    classDirectories.setFrom(files(asciiMainClasses))
    sourceDirectories.setFrom(sourceSets.main.java.srcDirs)
    reports {
        html.required = true
        xml.required  = false
    }
}
```

### 리포트 위치

```
build/reports/jacoco/test/html/index.html
```

---

## 2. 역할 분리 grep 검증

### 2-1. Controller에 `System.out` 직접 호출 없음

```bash
grep -rn "System\.out" src/main/java/org/example/controller/
# 결과 없음이어야 한다
```

**현재 상태**: `SampleController.java` 전체 확인 결과 `System.out` 직접 호출 없음 — 통과

### 2-2. Model에 `Scanner` 또는 콘솔 I/O 코드 없음

```bash
grep -rn "Scanner\|System" src/main/java/org/example/model/
# 결과 없음이어야 한다
```

### 2-3. View에 비즈니스 로직(조건 분기·계산) 없음

코드 리뷰 기준:

| 확인 항목 | 판단 기준 |
|-----------|-----------|
| `if` / `else` 비즈니스 조건 분기 | 포맷 분기(삼항 포함)만 허용, 도메인 판단 금지 |
| 계산식 (`+`, `-`, `*`, `/`) | 포맷 연산 외 도메인 계산 금지 |
| Repository·Model 직접 호출 | 금지 |

**현재 `SampleView.java` 체크 결과**  
- `forEach` 루프: 출력 반복만 수행 — 허용  
- `String.format`: 포맷팅 전용 — 허용  
- 조건 분기 없음, 계산식 없음, Repository 참조 없음 — 통과

---

## 3. `./gradlew test` 전 테스트 통과 확인

```bash
./gradlew test
```

| 테스트 클래스 | 케이스 수 | 기준 |
|--------------|-----------|------|
| `SampleTest` | 9 | 전부 PASS |
| `InMemorySampleRepositoryTest` | 9 | 전부 PASS |
| `SampleControllerTest` | 12 이상 | 전부 PASS |
| `SampleViewTest` | 6 이상 | 전부 PASS |
| `SampleFlowIntegrationTest` | 8 | 전부 PASS |
| `RouterTest` | 8 | 전부 PASS |

---

## 4. JaCoCo 커버리지 측정

```bash
./gradlew jacocoTestReport
```

### 커버리지 목표

| 레이어 | 대상 클래스 | 목표 |
|--------|------------|------|
| Model (entity) | `Sample` | 80% 이상 |
| Model (repository) | `InMemorySampleRepository` | 80% 이상 |
| Controller | `SampleController` | 80% 이상 |
| View | `SampleView` | 측정 (목표치 없음) |
| App | `Router` | 측정 (목표치 없음) |

### JaCoCo 최소 커버리지 강제 (선택)

커버리지 기준을 빌드 실패 조건으로 강제하려면 아래 블록을 추가한다:

```groovy
jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = 'PACKAGE'
            includes = ['org.example.model.*', 'org.example.controller.*']
            limit {
                counter = 'LINE'
                value   = 'COVEREDRATIO'
                minimum = 0.80
            }
        }
    }
}

// check.dependsOn jacocoTestCoverageVerification  // 선택 사항 — 활성화 시 build도 커버리지 미달로 실패
```

> Phase 5에서는 리포트 확인 후 수치를 파악하는 것이 1차 목표이며, 강제 적용은 선택 사항이다.  
> `check.dependsOn` 주석을 해제하면 `./gradlew build`가 커버리지 미달 시 실패하므로 PRD 수용 기준 §1과 충돌하지 않도록 80% 달성을 먼저 확인한 뒤 활성화한다.

---

## 5. PRD 수용 기준 최종 점검

| # | 수용 기준 (PRD §7) | 검증 방법 | 상태 |
|---|-------------------|-----------|------|
| 1 | `./gradlew build`가 경고 없이 성공 | `./gradlew build` 실행 출력 확인 | - |
| 2 | F-01~F-07 기능이 콘솔에서 정상 동작 | `./gradlew run` 수동 시연 | - |
| 3 | Controller에 `System.out` 직접 호출 없음 | §2-1 grep | - |
| 4 | Model에 `Scanner` 또는 콘솔 I/O 없음 | §2-2 grep | - |
| 5 | View에 비즈니스 로직 없음 | §2-3 코드 리뷰 | - |
| 6 | 잘못된 입력 시 오류 메시지 출력 후 재입력 | 통합 시나리오 테스트 | - |
| 7 | `./gradlew test` 전 테스트 통과 | §3 테스트 실행 | - |
| 8 | Model·Controller 커버리지 80% 이상 | §4 JaCoCo 리포트 | - |

---

## 6. 파일 목록

| 파일 | 작업 |
|------|------|
| `build.gradle` | `jacoco` 플러그인 추가 + `jacocoTestReport` 태스크 설정 |

> Phase 5는 소스 코드 변경 없이 `build.gradle` 수정과 검증 실행만으로 완료한다.  
> grep 또는 커버리지에서 위반이 발견될 경우 해당 Phase의 구현으로 되돌아가 수정한다.

---

## 7. 완료 기준

- [ ] `./gradlew build` 경고 없이 성공
- [ ] Controller grep — `System.out` 직접 호출 0건
- [ ] Model grep — `Scanner` / 콘솔 I/O 0건
- [ ] View 코드 리뷰 — 비즈니스 로직 없음 확인
- [ ] `./gradlew test` — 전 테스트 BUILD SUCCESSFUL
- [ ] `./gradlew jacocoTestReport` — `build/reports/jacoco/test/html/index.html` 생성
- [ ] Model·Controller LINE 커버리지 80% 이상 확인
- [ ] PRD 수용 기준 8개 항목 전부 충족
