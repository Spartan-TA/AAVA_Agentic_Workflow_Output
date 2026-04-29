package com.example.ems.repository;

import com.example.ems.entity.SchedulingSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulingSuggestionRepository extends JpaRepository<SchedulingSuggestion, Long> {
    // Custom query methods if needed
}
