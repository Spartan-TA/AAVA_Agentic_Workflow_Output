package com.example.app.controller;

import com.example.app.dto.SearchResultDto;
import com.example.app.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/users")
    public ResponseEntity<List<SearchResultDto>> searchUsers(@RequestParam String query) {
        List<SearchResultDto> results = searchService.searchUsers(query);
        return ResponseEntity.ok(results);
    }
}
