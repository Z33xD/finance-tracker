package com.fintrack.finance_tracker.import_batches;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImportBatchRepository extends JpaRepository<ImportBatch, Integer> {
    void deleteById(int id);
    Optional<ImportBatch> findById(int id);
}
