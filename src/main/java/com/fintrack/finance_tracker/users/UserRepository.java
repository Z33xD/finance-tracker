package com.fintrack.finance_tracker.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    void deleteById(int id);
    Optional<User> findById(int id);
}
