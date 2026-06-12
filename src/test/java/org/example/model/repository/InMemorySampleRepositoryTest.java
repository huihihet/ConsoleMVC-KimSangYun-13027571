package org.example.model.repository;

import org.example.model.entity.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemorySampleRepositoryTest {

    private InMemorySampleRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemorySampleRepository();
    }

    @Test
    void save_후_findById_필드_일치_ID_자동_할당() {
        Sample saved = repo.save(new Sample("AlphaChip", 30, 0.95, 100));

        assertEquals(1L,          saved.getSampleId());
        assertEquals("AlphaChip", saved.getName());
        assertEquals(30,          saved.getAvgProductionTime());
        assertEquals(0.95,        saved.getYield());
        assertEquals(100,         saved.getStock());

        Optional<Sample> found = repo.findById(1L);
        assertTrue(found.isPresent());
        assertEquals("AlphaChip", found.get().getName());
    }

    @Test
    void save_두_번_ID_자동_증가() {
        Sample first  = repo.save(new Sample("AlphaChip", 30, 0.95, 100));
        Sample second = repo.save(new Sample("BetaChip",  20, 0.80, 50));

        assertEquals(1L, first.getSampleId());
        assertEquals(2L, second.getSampleId());
    }

    @Test
    void findAll_빈_저장소_빈_리스트_반환() {
        List<Sample> result = repo.findAll();
        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_복수_저장_저장_순서대로_반환() {
        repo.save(new Sample("AlphaChip", 30, 0.95, 100));
        repo.save(new Sample("BetaChip",  20, 0.80, 50));

        List<Sample> result = repo.findAll();
        assertEquals(2,           result.size());
        assertEquals("AlphaChip", result.get(0).getName());
        assertEquals("BetaChip",  result.get(1).getName());
    }

    @Test
    void findById_미존재_Optional_empty_반환() {
        Optional<Sample> result = repo.findById(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByNameContaining_키워드_포함_해당_시료만_반환() {
        repo.save(new Sample("AlphaChip", 30, 0.95, 100));
        repo.save(new Sample("BetaChip",  20, 0.80, 50));

        List<Sample> result = repo.findByNameContaining("Alpha");
        assertEquals(1,           result.size());
        assertEquals("AlphaChip", result.get(0).getName());
    }

    @Test
    void findByNameContaining_키워드_미포함_빈_리스트_반환() {
        repo.save(new Sample("AlphaChip", 30, 0.95, 100));

        List<Sample> result = repo.findByNameContaining("Gamma");
        assertTrue(result.isEmpty());
    }

    @Test
    void findByNameContaining_빈_문자열_전체_반환() {
        repo.save(new Sample("AlphaChip", 30, 0.95, 100));
        repo.save(new Sample("BetaChip",  20, 0.80, 50));

        List<Sample> result = repo.findByNameContaining("");
        assertEquals(2, result.size());
    }

    @Test
    void update_존재_ID_true_반환_변경_내용_확인() {
        repo.save(new Sample("AlphaChip", 30, 0.95, 100));

        Sample updated = new Sample(1L, "AlphaChip-v2", 25, 0.90, 200);
        boolean result = repo.update(updated);

        assertTrue(result);
        Optional<Sample> found = repo.findById(1L);
        assertTrue(found.isPresent());
        assertEquals("AlphaChip-v2", found.get().getName());
        assertEquals(25,             found.get().getAvgProductionTime());
        assertEquals(0.90,           found.get().getYield());
        assertEquals(200,            found.get().getStock());
    }

    @Test
    void update_미존재_ID_false_반환() {
        Sample ghost = new Sample(999L, "Ghost", 10, 0.5, 0);
        boolean result = repo.update(ghost);
        assertFalse(result);
    }

    @Test
    void deleteById_존재_ID_true_반환_후_Optional_empty() {
        repo.save(new Sample("AlphaChip", 30, 0.95, 100));

        boolean result = repo.deleteById(1L);
        assertTrue(result);
        assertTrue(repo.findById(1L).isEmpty());
    }

    @Test
    void deleteById_미존재_ID_false_반환() {
        boolean result = repo.deleteById(999L);
        assertFalse(result);
    }

    @Test
    void findByNameContaining_null_전체_반환() {
        repo.save(new Sample("AlphaChip", 30, 0.95, 100));
        repo.save(new Sample("BetaChip",  20, 0.80, 50));

        List<Sample> result = repo.findByNameContaining(null);
        assertEquals(2, result.size());
    }

    @Test
    void findAll_방어적_복사_내부_store_불변() {
        repo.save(new Sample("AlphaChip", 30, 0.95, 100));

        List<Sample> result = repo.findAll();
        result.clear();

        assertEquals(1, repo.findAll().size());
    }
}
