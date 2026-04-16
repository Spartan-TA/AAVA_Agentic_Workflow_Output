package com.example.mcqassessment.repository;

import com.example.mcqassessment.domain.AnswerChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerChoiceRepository extends JpaRepository<AnswerChoice, Long> {
}
