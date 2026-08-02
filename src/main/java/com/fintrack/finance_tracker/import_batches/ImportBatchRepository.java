package com.fintrack.finance_tracker.import_batches;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImportBatchRepository extends JpaRepository<ImportBatch, Integer> {
    void deleteById(int id);

    @Modifying
    @Query("DELETE FROM ImportBatch b WHERE b.id = :id AND b.user_id = :userId")
    void deleteByIdAndUserId(@Param("id") int id, @Param("userId") int userId);

    Optional<ImportBatch> findById(int id);

    @Query("SELECT b FROM ImportBatch b WHERE b.id = :id AND b.user_id = :userId")
    Optional<ImportBatch> findByIdAndUserId(@Param("id") int id, @Param("userId") int userId);

    @Query("SELECT b FROM ImportBatch b WHERE b.user_id = :userId")
    List<ImportBatch> findByUserId(@Param("userId") int userId);
}
