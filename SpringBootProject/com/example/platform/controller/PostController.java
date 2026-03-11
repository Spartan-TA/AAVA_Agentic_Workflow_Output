package com.example.platform.controller;

import com.example.platform.dto.PostDto;
import com.example.platform.dto.CreatePostRequest;
import com.example.platform.dto.EditPostRequest;
import com.example.platform.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@Validated
public class PostController {
    @Autowired
    private PostService postService;

    @GetMapping
    public ResponseEntity<List<PostDto>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPostById(postId));
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(@AuthenticationPrincipal(expression = "id") Long userId,
                                              @Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(postService.createPost(userId, request));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostDto> editPost(@PathVariable Long postId,
                                            @AuthenticationPrincipal(expression = "id") Long userId,
                                            @Valid @RequestBody EditPostRequest request) {
        return ResponseEntity.ok(postService.editPost(postId, userId, request));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId,
                                           @AuthenticationPrincipal(expression = "id") Long userId) {
        postService.deletePost(postId, userId);
        return ResponseEntity.ok().build();
    }
}
