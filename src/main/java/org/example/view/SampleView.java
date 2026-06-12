package org.example.view;

import org.example.model.entity.Sample;
import java.util.List;

public class SampleView {

    private static final String SEP = "=".repeat(50);

    public void printMenu() {
        System.out.println(SEP);
        System.out.println("  S-Semi 시료 관리 시스템");
        System.out.println(SEP);
        System.out.println("1. 시료 등록");
        System.out.println("2. 시료 목록 조회");
        System.out.println("3. 시료 조회 (ID)");
        System.out.println("4. 시료 수정");
        System.out.println("5. 시료 삭제");
        System.out.println("6. 이름 검색");
        System.out.println("0. 종료");
        System.out.print("선택 > ");
    }

    // Controller가 System.out 직접 호출 금지 규칙을 지키기 위한 프롬프트 위임
    public void printPrompt(String prompt) {
        System.out.print(prompt);
    }

    public void printUpdateFieldMenu() {
        System.out.println("1. 이름  2. 평균 생산 시간  3. 수율  4. 재고");
    }

    public void printSampleList(List<Sample> samples) {
        System.out.println("ID  | 이름            | 생산시간(min) | 수율  | 재고");
        System.out.println("----|-----------------|--------------|-------|-----");
        samples.forEach(s -> System.out.println(
                String.format("%-4d| %-15s| %-13d| %-6.2f| %d",
                        s.getSampleId(), s.getName(),
                        s.getAvgProductionTime(), s.getYield(), s.getStock())
        ));
    }

    public void printSampleDetail(Sample sample) {
        System.out.println("=== 시료 상세 ===");
        System.out.println("ID           : " + sample.getSampleId());
        System.out.println("이름         : " + sample.getName());
        System.out.println("평균생산시간 : " + sample.getAvgProductionTime() + "min");
        System.out.println("수율         : " + sample.getYield());
        System.out.println("재고         : " + sample.getStock() + "개");
    }

    public void printSuccess(String message) {
        System.out.println("[성공] " + message);
    }

    public void printError(String message) {
        System.out.println("[오류] " + message);
    }

    public void printEmpty() {
        System.out.println("조회 결과가 없습니다.");
    }
}
