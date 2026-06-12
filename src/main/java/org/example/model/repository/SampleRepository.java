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
