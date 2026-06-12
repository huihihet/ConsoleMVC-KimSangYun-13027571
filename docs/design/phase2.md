# Phase 2 설계 — Model 구현

> 기준: `docs/PLAN.md > Phase 2` / PRD 마일스톤: M2  
> 목표: `Sample` 엔티티와 `InMemorySampleRepository`를 구현하고 단위 테스트를 작성한다.

---

## 1. 전환 절차 (Phase 1 → Phase 2)

`SampleRepository` 인터페이스에 메서드를 추가하면 `InMemorySampleRepository`의 컴파일이 즉시 실패한다.
아래 순서를 반드시 지킨다:

1. `SampleRepository`에 메서드 시그니처 추가
2. `InMemorySampleRepository`에 빈 구현(`throw new UnsupportedOperationException()`) 추가 → 컴파일 통과 확인
3. 각 메서드 구현 내용을 채운다
4. 테스트를 작성한다
5. 각 테스트 클래스의 `placeholder()` 메서드를 삭제하고 실제 케이스로 교체한다

---

## 2. `Sample` 엔티티 설계

### 필드

```java
package org.example.model.entity;

public class Sample {
    private Long   sampleId;
    private String name;
    private int    avgProductionTime;
    private double yield;
    private int    stock;
}
```

### 생성자 — 신규 등록용 (sampleId 없음)

```java
public Sample(String name, int avgProductionTime, double yield, int stock) {
    validate(name, avgProductionTime, yield, stock);
    this.name              = name;
    this.avgProductionTime = avgProductionTime;
    this.yield             = yield;
    this.stock             = stock;
}
```

### 생성자 — Repository 복원용 (sampleId 포함)

```java
public Sample(Long sampleId, String name, int avgProductionTime, double yield, int stock) {
    validate(name, avgProductionTime, yield, stock);
    this.sampleId          = sampleId;
    this.name              = name;
    this.avgProductionTime = avgProductionTime;
    this.yield             = yield;
    this.stock             = stock;
}
```

### 유효성 검증 규칙

| 필드 | 조건 | 위반 시 |
|------|------|---------|
| `name` | null 아님, 공백·빈 문자열 아님 | `IllegalArgumentException` |
| `avgProductionTime` | 1 이상 | `IllegalArgumentException` |
| `yield` | 0.0 초과 ~ 1.0 이하 | `IllegalArgumentException` |
| `stock` | 0 이상 | `IllegalArgumentException` |

```java
private void validate(String name, int avgProductionTime, double yield, int stock) {
    if (name == null || name.isBlank())
        throw new IllegalArgumentException("이름은 공백일 수 없습니다.");
    if (avgProductionTime < 1)
        throw new IllegalArgumentException("평균 생산 시간은 1 이상이어야 합니다.");
    if (yield <= 0.0 || yield > 1.0)
        throw new IllegalArgumentException("수율은 0.0 초과 1.0 이하여야 합니다.");
    if (stock < 0)
        throw new IllegalArgumentException("재고 수량은 0 이상이어야 합니다.");
}
```

### 수정 메서드

```java
public void updateName(String name)                        // 동일 유효성 검증
public void updateAvgProductionTime(int avgProductionTime) // 동일 유효성 검증
public void updateYield(double yield)                      // 동일 유효성 검증
public void updateStock(int stock)                         // 동일 유효성 검증
```

### Getter / toString

- 모든 필드 getter 제공 (`getId()`, `getName()` 등)
- `sampleId` setter 없음 — Repository가 `save()` 시점에 ID를 할당한다
- `toString()`: `[ID] name | 생산시간: Nmin | 수율: 0.XX | 재고: N개` 형식

---

## 3. `SampleRepository` 인터페이스 설계

```java
package org.example.model.repository;

import org.example.model.entity.Sample;
import java.util.List;
import java.util.Optional;

public interface SampleRepository {
    Sample           save(Sample sample);
    List<Sample>     findAll();
    Optional<Sample> findById(Long id);
    List<Sample>     findByNameContaining(String keyword);
    boolean          update(Sample sample);
    boolean          deleteById(Long id);
}
```

---

## 4. `InMemorySampleRepository` 구현체 설계

```java
package org.example.model.repository;

import org.example.model.entity.Sample;
import java.util.*;

public class InMemorySampleRepository implements SampleRepository {
    private final List<Sample> store   = new ArrayList<>();
    private       long         nextId  = 1L;
}
```

### 메서드별 구현 전략

| 메서드 | 구현 전략 |
|--------|-----------|
| `save(sample)` | `nextId`를 `sampleId`로 할당 후 `store`에 추가, `nextId++`, 저장된 객체 반환 |
| `findAll()` | `new ArrayList<>(store)` 방어적 복사 반환 |
| `findById(id)` | `store.stream().filter(s -> s.getId().equals(id)).findFirst()` |
| `findByNameContaining(keyword)` | `keyword`가 빈 문자열이면 `findAll()` 반환, 아니면 `name.contains(keyword)` 필터링 |
| `update(sample)` | `findById`로 기존 객체 찾아 필드 교체, 없으면 `false` 반환 |
| `deleteById(id)` | `store.removeIf(s -> s.getId().equals(id))` |

> `sampleId` 할당 방식: `save()` 호출 시 `nextId`를 ID로 설정하는 별도 내부 메서드를 사용한다.
> `Sample`에 `sampleId` setter를 노출하지 않으므로 ID가 할당된 새 `Sample` 객체를 생성해 교체한다.

---

## 5. 단위 테스트 설계

### 5-1. 공통 픽스처

```java
// SampleTest, InMemorySampleRepositoryTest 모두 적용
@BeforeEach
void setUp() {
    // SampleTest: 정상 Sample 인스턴스 준비
    sample = new Sample("AlphaChip", 30, 0.95, 100);

    // InMemorySampleRepositoryTest: 매 테스트마다 새 인스턴스
    repo = new InMemorySampleRepository();
}
```

### 5-2. `SampleTest` 케이스

| 케이스 | 검증 |
|--------|------|
| 정상 생성 | 모든 필드 값 일치 |
| `yield = 0.0` | `IllegalArgumentException` |
| `yield = 1.0` | 정상 생성 (상한 경계값 허용) |
| `yield > 1.0` | `IllegalArgumentException` |
| `yield < 0.0` | `IllegalArgumentException` |
| `avgProductionTime = 0` | `IllegalArgumentException` |
| `avgProductionTime = 1` | 정상 생성 (하한 경계값 허용) |
| `stock = -1` | `IllegalArgumentException` |
| `stock = 0` | 정상 생성 |
| `name = ""` | `IllegalArgumentException` |
| `name = "  "` (공백만) | `IllegalArgumentException` |
| `name = null` | `IllegalArgumentException` |
| `updateYield(0.0)` | `IllegalArgumentException` |
| `updateName("")` | `IllegalArgumentException` |

### 5-3. `InMemorySampleRepositoryTest` 케이스

| 케이스 | 검증 |
|--------|------|
| `save` 후 `findById` | 반환 객체의 모든 필드 일치, ID 자동 할당 |
| `save` 두 번 → ID 자동 증가 | 첫 번째 ID=1, 두 번째 ID=2 |
| `findAll` 빈 저장소 | 빈 리스트 반환 |
| `findAll` 복수 저장 | 저장 순서대로 반환 |
| `findById` 미존재 | `Optional.empty()` 반환 |
| `findByNameContaining` 키워드 포함 | 해당 시료만 반환 |
| `findByNameContaining` 키워드 미포함 | 빈 리스트 반환 |
| `findByNameContaining("")` 빈 문자열 | 전체 시료 반환 |
| `update` 존재하는 ID | `true` 반환 + `findById`로 변경 내용 확인 |
| `update` 미존재 ID | `false` 반환 |
| `deleteById` 존재하는 ID | `true` 반환 + `findById` → `Optional.empty()` |
| `deleteById` 미존재 ID | `false` 반환 |

---

## 6. 완료 기준

- [ ] `Sample` 유효성 검증 전 케이스 동작
- [ ] `InMemorySampleRepository` CRUD 전 케이스 동작
- [ ] `./gradlew test` 전 테스트 통과
- [ ] `./gradlew jacocoTestReport` Model 커버리지 80% 이상
- [ ] Model 클래스에 `Scanner`, `System.out` 코드 없음
- [ ] Phase 3 시작 전 `build.gradle`에 Mockito 5.x 의존성 추가 (`PLAN.md Phase 3-0` 참고)
