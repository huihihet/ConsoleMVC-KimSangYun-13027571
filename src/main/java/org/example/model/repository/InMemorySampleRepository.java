package org.example.model.repository;

import org.example.model.entity.Sample;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemorySampleRepository implements SampleRepository {
    private final List<Sample> store  = new ArrayList<>();
    private       long         nextId = 1L;

    @Override
    public Sample save(Sample sample) {
        Sample saved = new Sample(
                nextId++,
                sample.getName(),
                sample.getAvgProductionTime(),
                sample.getYield(),
                sample.getStock()
        );
        store.add(saved);
        return saved;
    }

    @Override
    public List<Sample> findAll() {
        return new ArrayList<>(store);
    }

    @Override
    public Optional<Sample> findById(Long id) {
        return store.stream()
                .filter(s -> s.getSampleId().equals(id))
                .findFirst();
    }

    @Override
    public List<Sample> findByNameContaining(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        return store.stream()
                .filter(s -> s.getName().contains(keyword))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public boolean update(Sample sample) {
        for (int i = 0; i < store.size(); i++) {
            if (store.get(i).getSampleId().equals(sample.getSampleId())) {
                store.set(i, sample);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean deleteById(Long id) {
        return store.removeIf(s -> s.getSampleId().equals(id));
    }
}
