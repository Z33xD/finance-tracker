package com.fintrack.finance_tracker.categories;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    void deleteById(int id);
    Optional<Category> findById(int id);
}
