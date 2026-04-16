package com.example.mcqassessment.repository;

import com.example.mcqassessment.domain.AssessmentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, Long> {
}
