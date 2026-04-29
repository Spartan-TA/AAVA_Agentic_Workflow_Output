package com.example.ems.service;

import com.example.ems.entity.SchedulingSuggestion;
import com.example.ems.repository.SchedulingSuggestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SchedulingSuggestionService {

    @Autowired
    private SchedulingSuggestionRepository schedulingSuggestionRepository;

    public List<SchedulingSuggestion> getAllSuggestions() {
        return schedulingSuggestionRepository.findAll();
    }

    public Optional<SchedulingSuggestion> getSuggestionById(Long id) {
        return schedulingSuggestionRepository.findById(id);
    }

    public SchedulingSuggestion createSuggestion(SchedulingSuggestion suggestion) {
        return schedulingSuggestionRepository.save(suggestion);
    }

    public SchedulingSuggestion updateSuggestion(Long id, SchedulingSuggestion updatedSuggestion) {
        return schedulingSuggestionRepository.findById(id)
                .map(existing -> {
                    existing.setSuggestionType(updatedSuggestion.getSuggestionType());
                    existing.setDescription(updatedSuggestion.getDescription());
                    existing.setCreatedAt(updatedSuggestion.getCreatedAt());
                    existing.setStatus(updatedSuggestion.getStatus());
                    existing.setActor(updatedSuggestion.getActor());
                    return schedulingSuggestionRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("SchedulingSuggestion not found"));
    }

    public void deleteSuggestion(Long id) {
        schedulingSuggestionRepository.deleteById(id);
    }
}
