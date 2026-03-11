package com.example.app.service;

import com.example.app.dto.SearchResultDto;
import java.util.List;

public interface SearchService {
    List<SearchResultDto> searchUsers(String query);
}
