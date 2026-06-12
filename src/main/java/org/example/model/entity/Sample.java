package org.example.model.entity;

public class Sample {
    private Long   sampleId;
    private String name;
    private int    avgProductionTime;
    private double yield;
    private int    stock;

    // 신규 등록용 — Repository가 save() 시점에 sampleId를 할당한다
    public Sample(String name, int avgProductionTime, double yield, int stock) {
        validate(name, avgProductionTime, yield, stock);
        this.name              = name;
        this.avgProductionTime = avgProductionTime;
        this.yield             = yield;
        this.stock             = stock;
    }

    // Repository 복원용 — sampleId가 이미 확정된 경우에만 사용한다
    public Sample(Long sampleId, String name, int avgProductionTime, double yield, int stock) {
        validate(name, avgProductionTime, yield, stock);
        this.sampleId          = sampleId;
        this.name              = name;
        this.avgProductionTime = avgProductionTime;
        this.yield             = yield;
        this.stock             = stock;
    }

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

    public void updateName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("이름은 공백일 수 없습니다.");
        this.name = name;
    }

    public void updateAvgProductionTime(int avgProductionTime) {
        if (avgProductionTime < 1)
            throw new IllegalArgumentException("평균 생산 시간은 1 이상이어야 합니다.");
        this.avgProductionTime = avgProductionTime;
    }

    public void updateYield(double yield) {
        if (yield <= 0.0 || yield > 1.0)
            throw new IllegalArgumentException("수율은 0.0 초과 1.0 이하여야 합니다.");
        this.yield = yield;
    }

    public void updateStock(int stock) {
        if (stock < 0)
            throw new IllegalArgumentException("재고 수량은 0 이상이어야 합니다.");
        this.stock = stock;
    }

    public Long   getSampleId()          { return sampleId; }
    public String getName()              { return name; }
    public int    getAvgProductionTime() { return avgProductionTime; }
    public double getYield()             { return yield; }
    public int    getStock()             { return stock; }

    @Override
    public String toString() {
        return "[" + sampleId + "] " + name
                + " | 생산시간: " + avgProductionTime + "min"
                + " | 수율: " + yield
                + " | 재고: " + stock + "개";
    }
}
