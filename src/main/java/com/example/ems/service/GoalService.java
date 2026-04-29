package com.example.ems.service;

import com.example.ems.entity.Goal;
import com.example.ems.repository.GoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GoalService {

    @Autowired
    private GoalRepository goalRepository;

    public List<Goal> getAllGoals() {
        return goalRepository.findAll();
    }

    public Optional<Goal> getGoalById(Long id) {
        return goalRepository.findById(id);
    }

    public Goal createGoal(Goal goal) {
        return goalRepository.save(goal);
    }

    public Goal updateGoal(Long id, Goal updatedGoal) {
        return goalRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(updatedGoal.getTitle());
                    existing.setDescription(updatedGoal.getDescription());
                    existing.setRating(updatedGoal.getRating());
                    existing.setStatus(updatedGoal.getStatus());
                    return goalRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Goal not found"));
    }

    public void deleteGoal(Long id) {
        goalRepository.deleteById(id);
    }
}
