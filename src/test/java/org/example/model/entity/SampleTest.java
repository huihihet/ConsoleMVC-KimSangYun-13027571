package org.example.model.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SampleTest {

    private Sample sample;

    @BeforeEach
    void setUp() {
        sample = new Sample("AlphaChip", 30, 0.95, 100);
    }

    @Test
    void 정상_생성_모든_필드_일치() {
        assertEquals("AlphaChip", sample.getName());
        assertEquals(30,   sample.getAvgProductionTime());
        assertEquals(0.95, sample.getYield());
        assertEquals(100,  sample.getStock());
        assertNull(sample.getSampleId());
    }

    @Test
    void yield_0_0_은_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> new Sample("AlphaChip", 30, 0.0, 100));
    }

    @Test
    void yield_1_0_은_정상_생성() {
        Sample s = new Sample("AlphaChip", 30, 1.0, 100);
        assertEquals(1.0, s.getYield());
    }

    @Test
    void yield_1_0_초과_는_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> new Sample("AlphaChip", 30, 1.1, 100));
    }

    @Test
    void yield_0_0_미만_은_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> new Sample("AlphaChip", 30, -0.1, 100));
    }

    @Test
    void avgProductionTime_0_은_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> new Sample("AlphaChip", 0, 0.95, 100));
    }

    @Test
    void avgProductionTime_1_은_정상_생성() {
        Sample s = new Sample("AlphaChip", 1, 0.95, 100);
        assertEquals(1, s.getAvgProductionTime());
    }

    @Test
    void stock_음수_는_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> new Sample("AlphaChip", 30, 0.95, -1));
    }

    @Test
    void stock_0_은_정상_생성() {
        Sample s = new Sample("AlphaChip", 30, 0.95, 0);
        assertEquals(0, s.getStock());
    }

    @Test
    void name_빈_문자열_은_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> new Sample("", 30, 0.95, 100));
    }

    @Test
    void name_공백만_있는_문자열_은_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> new Sample("  ", 30, 0.95, 100));
    }

    @Test
    void name_null_은_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> new Sample(null, 30, 0.95, 100));
    }

    @Test
    void updateYield_0_0_은_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> sample.updateYield(0.0));
    }

    @Test
    void updateYield_1_1_은_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> sample.updateYield(1.1));
    }

    @Test
    void updateName_빈_문자열_은_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> sample.updateName(""));
    }

    @Test
    void updateName_null_은_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> sample.updateName(null));
    }

    @Test
    void updateAvgProductionTime_0_은_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> sample.updateAvgProductionTime(0));
    }

    @Test
    void updateAvgProductionTime_정상_변경() {
        sample.updateAvgProductionTime(60);
        assertEquals(60, sample.getAvgProductionTime());
    }

    @Test
    void updateStock_음수_는_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> sample.updateStock(-1));
    }

    @Test
    void updateStock_정상_변경() {
        sample.updateStock(200);
        assertEquals(200, sample.getStock());
    }

    @Test
    void toString_형식_확인() {
        sample.updateStock(100);
        String result = sample.toString();
        assertTrue(result.contains("AlphaChip"));
        assertTrue(result.contains("30"));
        assertTrue(result.contains("0.95"));
        assertTrue(result.contains("100"));
    }
}
