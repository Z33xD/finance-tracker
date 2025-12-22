package com.fintrack.finance_tracker.budgets;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {
    void deleteById(int id);
    Optional<Budget> findById(int id);
}
