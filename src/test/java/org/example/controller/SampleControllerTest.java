package org.example.controller;

import org.example.model.entity.Sample;
import org.example.model.repository.InMemorySampleRepository;
import org.example.model.repository.SampleRepository;
import org.example.view.SampleView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SampleControllerTest {

    private final PrintStream originalOut = System.out;
    private SampleRepository repository;
    @Spy
    private SampleView view = new SampleView();
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        repository = new InMemorySampleRepository();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));
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

    // --- register ---

    @Test
    void register_정상_저장소에_저장되고_성공_메시지_출력() {
        controller("AlphaChip\n30\n0.95\n").register();
        assertEquals(1, repository.findAll().size());
        assertTrue(output().contains("[성공]"));
    }

    @Test
    void register_생산시간_파싱_실패_저장없고_오류_출력() {
        controller("AlphaChip\nabc\n0.95\n").register();
        assertTrue(repository.findAll().isEmpty());
        assertTrue(output().contains("[오류]"));
    }

    @Test
    void register_수율_파싱_실패_저장없고_오류_출력() {
        controller("AlphaChip\n30\nxyz\n").register();
        assertTrue(repository.findAll().isEmpty());
        assertTrue(output().contains("[오류]"));
    }

    @Test
    void register_유효성_실패_yield_0_저장없고_오류_출력() {
        controller("AlphaChip\n30\n0.0\n").register();
        assertTrue(repository.findAll().isEmpty());
        assertTrue(output().contains("[오류]"));
    }

    // --- listAll ---

    @Test
    void listAll_빈_저장소_안내_메시지_출력() {
        controller("").listAll();
        assertTrue(output().contains("조회 결과가 없습니다."));
    }

    @Test
    void listAll_데이터_있으면_시료_이름_출력() {
        repository.save(new Sample("AlphaChip", 30, 0.95, 100));
        controller("").listAll();
        assertTrue(output().contains("AlphaChip"));
    }

    // --- findById ---

    @Test
    void findById_존재_ID_상세_출력() {
        repository.save(new Sample("AlphaChip", 30, 0.95, 100));
        controller("1\n").findById();
        assertTrue(output().contains("=== 시료 상세 ==="));
    }

    @Test
    void findById_미존재_ID_오류_출력() {
        controller("999\n").findById();
        assertTrue(output().contains("[오류]"));
    }

    @Test
    void findById_ID_파싱_실패_오류_출력() {
        controller("abc\n").findById();
        assertTrue(output().contains("[오류]"));
    }

    // --- update ---

    @Test
    void update_이름_정상_수정_저장소에_반영() {
        repository.save(new Sample("AlphaChip", 30, 0.95, 100));
        // ID=1, 필드선택=1(이름), 새이름=BetaChip
        controller("1\n1\nBetaChip\n").update();
        assertEquals("BetaChip", repository.findById(1L).get().getName());
    }

    @Test
    void update_ID_파싱_실패_오류_출력() {
        controller("abc\n").update();
        assertTrue(output().contains("[오류]"));
    }

    @Test
    void update_미존재_ID_오류_출력() {
        controller("999\n").update();
        assertTrue(output().contains("[오류]"));
    }

    @Test
    void update_생산시간_정상_수정_저장소에_반영() {
        repository.save(new Sample("AlphaChip", 30, 0.95, 100));
        controller("1\n2\n60\n").update();
        assertEquals(60, repository.findById(1L).get().getAvgProductionTime());
    }

    @Test
    void update_수율_정상_수정_저장소에_반영() {
        repository.save(new Sample("AlphaChip", 30, 0.95, 100));
        controller("1\n3\n0.80\n").update();
        assertEquals(0.80, repository.findById(1L).get().getYield(), 0.001);
    }

    @Test
    void update_재고_정상_수정_저장소에_반영() {
        repository.save(new Sample("AlphaChip", 30, 0.95, 100));
        controller("1\n4\n200\n").update();
        assertEquals(200, repository.findById(1L).get().getStock());
    }

    @Test
    void update_유효하지_않은_필드_번호_오류_출력() {
        repository.save(new Sample("AlphaChip", 30, 0.95, 100));
        controller("1\n9\n").update();
        assertTrue(output().contains("[오류]"));
    }

    // --- delete ---

    @Test
    void delete_존재_ID_삭제되고_성공_메시지() {
        repository.save(new Sample("AlphaChip", 30, 0.95, 100));
        controller("1\n").delete();
        assertTrue(repository.findAll().isEmpty());
        assertTrue(output().contains("[성공]"));
    }

    @Test
    void delete_미존재_ID_오류_출력() {
        controller("999\n").delete();
        assertTrue(output().contains("[오류]"));
    }

    @Test
    void delete_ID_파싱_실패_오류_출력() {
        controller("abc\n").delete();
        assertTrue(output().contains("[오류]"));
    }

    // --- searchByName ---

    @Test
    void searchByName_결과_있으면_목록_출력() {
        repository.save(new Sample("AlphaChip", 30, 0.95, 100));
        repository.save(new Sample("BetaChip",  20, 0.80,  50));
        controller("Alpha\n").searchByName();
        assertTrue(output().contains("AlphaChip"));
    }

    @Test
    void searchByName_결과_없으면_안내_메시지_출력() {
        repository.save(new Sample("AlphaChip", 30, 0.95, 100));
        controller("Gamma\n").searchByName();
        assertTrue(output().contains("조회 결과가 없습니다."));
    }

    // --- handleInvalidMenu ---

    @Test
    void handleInvalidMenu_오류_메시지_출력() {
        controller("").handleInvalidMenu();
        String out = output();
        assertTrue(out.contains("[오류]"));
        assertTrue(out.contains("유효하지 않은 메뉴입니다."));
    }
}
