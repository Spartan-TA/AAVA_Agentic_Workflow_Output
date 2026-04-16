package com.example.mcqassessment.repository;

import com.example.mcqassessment.domain.AttemptedQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttemptedQuestionRepository extends JpaRepository<AttemptedQuestion, Long> {
}
