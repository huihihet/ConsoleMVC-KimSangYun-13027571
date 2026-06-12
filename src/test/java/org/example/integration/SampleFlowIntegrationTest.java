package org.example.integration;

import org.example.Main;
import org.example.controller.SampleController;
import org.example.model.entity.Sample;
import org.example.model.repository.InMemorySampleRepository;
import org.example.model.repository.SampleRepository;
import org.example.app.Router;
import org.example.view.SampleView;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class SampleFlowIntegrationTest {

    private final PrintStream      originalOut = System.out;
    private final InputStream      originalIn  = System.in;
    private SampleRepository       repository;
    private SampleView             view;
    private ByteArrayOutputStream  outContent;

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
        System.setIn(originalIn);
    }

    private SampleController controller(String input) {
        return new SampleController(repository, view, new Scanner(input));
    }

    private String output() {
        return outContent.toString(StandardCharsets.UTF_8);
    }

    @Test
    void 등록_후_목록_조회_시료_이름_포함() {
        controller("AlphaChip\n30\n0.95\n").register();
        outContent.reset();
        controller("").listAll();
        assertTrue(output().contains("AlphaChip"));
    }

    @Test
    void 등록_수정_단건조회_수정된_이름_반영() {
        controller("AlphaChip\n30\n0.95\n").register();
        controller("1\n1\nBetaChip\n").update();
        outContent.reset();
        controller("1\n").findById();
        assertTrue(output().contains("BetaChip"));
    }

    @Test
    void 등록_삭제_단건조회_오류_메시지_출력() {
        controller("AlphaChip\n30\n0.95\n").register();
        controller("1\n").delete();
        outContent.reset();
        controller("1\n").findById();
        assertTrue(output().contains("[오류]"));
    }

    @Test
    void 복수_등록_이름_검색_키워드_포함_시료만_반환() {
        controller("AlphaChip\n30\n0.95\n").register();
        controller("BetaChip\n20\n0.80\n").register();
        outContent.reset();
        controller("Alpha\n").searchByName();
        String out = output();
        assertTrue(out.contains("AlphaChip"));
        assertFalse(out.contains("BetaChip"));
    }

    @Test
    void 잘못된_메뉴_번호_오류_메시지_출력() {
        Router router = new Router(controller(""));
        router.route(9);
        assertTrue(output().contains("[오류]"));
        assertTrue(output().contains("유효하지 않은 메뉴입니다."));
    }

    @Test
    void 메뉴_0_입력_시_false_반환() {
        Router router = new Router(controller(""));
        assertFalse(router.route(0));
    }

    @Test
    void Main_루프_숫자아닌_입력_오류_후_0으로_종료() {
        // 숫자 아닌 입력 → 오류 메시지 → 0 입력 → 종료
        System.setIn(new ByteArrayInputStream("abc\n0\n".getBytes(StandardCharsets.UTF_8)));
        Main.main(new String[]{});
        String out = output();
        assertTrue(out.contains("[오류]"));
        assertTrue(out.contains("메뉴는 숫자로 입력하세요."));
    }

    @Test
    void Main_루프_정상_등록_후_0으로_종료() {
        // 1(등록) → 입력 → 0(종료)
        String input = "1\nAlphaChip\n30\n0.95\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Main.main(new String[]{});
        assertTrue(output().contains("[성공]"));
    }
}
