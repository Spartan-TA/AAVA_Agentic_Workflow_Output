package com.example.app.service.impl;

import com.example.app.dto.SearchResultDto;
import com.example.app.entity.User;
import com.example.app.repository.UserRepository;
import com.example.app.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final UserRepository userRepository;

    @Override
    public List<SearchResultDto> searchUsers(String query) {
        List<User> users = userRepository.findAll().stream()
                .filter(user -> user.getUsername().contains(query) || user.getEmail().contains(query))
                .collect(Collectors.toList());
        return users.stream().map(SearchResultDto::fromEntity).collect(Collectors.toList());
    }
}
