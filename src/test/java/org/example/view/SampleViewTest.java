package org.example.view;

import org.example.model.entity.Sample;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SampleViewTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;
    private SampleView view;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));
        view = new SampleView();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private String output() {
        return outContent.toString(StandardCharsets.UTF_8);
    }

    @Test
    void printMenu_시료등록과_종료_문자열_포함() {
        view.printMenu();
        String out = output();
        assertTrue(out.contains("시료 등록"));
        assertTrue(out.contains("종료"));
    }

    @Test
    void printSampleList_단건_모든_필드_포함() {
        Sample sample = new Sample(1L, "AlphaChip", 30, 0.95, 100);
        view.printSampleList(List.of(sample));
        String out = output();
        assertTrue(out.contains("1"));
        assertTrue(out.contains("AlphaChip"));
        assertTrue(out.contains("30"));
        assertTrue(out.contains("0.95"));
        assertTrue(out.contains("100"));
    }

    @Test
    void printSampleList_빈_리스트_헤더만_출력() {
        view.printSampleList(Collections.emptyList());
        String out = output();
        assertTrue(out.contains("ID"));
        assertTrue(out.contains("이름"));
        // 시료 데이터 행 없음 — 헤더 두 줄만 존재
        long dataLines = out.lines()
                .filter(line -> line.matches("\\d+.*"))
                .count();
        assertTrue(dataLines == 0);
    }

    @Test
    void printSampleDetail_모든_필드_포함() {
        Sample sample = new Sample(1L, "AlphaChip", 30, 0.95, 100);
        view.printSampleDetail(sample);
        String out = output();
        assertTrue(out.contains("AlphaChip"));
        assertTrue(out.contains("30"));
        assertTrue(out.contains("0.95"));
        assertTrue(out.contains("100"));
    }

    @Test
    void printSuccess_성공_접두사와_메시지_포함() {
        view.printSuccess("테스트 성공 메시지");
        String out = output();
        assertTrue(out.contains("[성공]"));
        assertTrue(out.contains("테스트 성공 메시지"));
    }

    @Test
    void printError_오류_접두사와_메시지_포함() {
        view.printError("테스트 오류 메시지");
        String out = output();
        assertTrue(out.contains("[오류]"));
        assertTrue(out.contains("테스트 오류 메시지"));
    }

    @Test
    void printEmpty_안내_메시지_포함() {
        view.printEmpty();
        assertTrue(output().contains("조회 결과가 없습니다."));
    }

    @Test
    void printPrompt_줄바꿈_없이_메시지_출력() {
        view.printPrompt("이름: ");
        assertTrue(output().contains("이름: "));
    }

    @Test
    void printUpdateFieldMenu_필드_선택_항목_포함() {
        view.printUpdateFieldMenu();
        String out = output();
        assertTrue(out.contains("이름"));
        assertTrue(out.contains("수율"));
        assertTrue(out.contains("재고"));
    }
}
