package com.example.platform.controller;

import com.example.platform.dto.SearchResultDto;
import com.example.platform.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    @Autowired
    private SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResultDto> search(@RequestParam String query) {
        return ResponseEntity.ok(searchService.search(query));
    }
}
