package com.example.platform.service;

import com.example.platform.dto.PostDto;
import com.example.platform.dto.CreatePostRequest;
import com.example.platform.dto.EditPostRequest;
import com.example.platform.dto.CommentDto;
import com.example.platform.entity.Post;
import com.example.platform.entity.User;
import com.example.platform.repository.PostRepository;
import com.example.platform.repository.UserRepository;
import com.example.platform.repository.CommentRepository;
import com.example.platform.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private LikeRepository likeRepository;

    public List<PostDto> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public PostDto getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found."));
        return toDto(post);
    }

    @Transactional
    public PostDto createPost(Long authorId, CreatePostRequest request) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        Post post = new Post();
        post.setContent(request.getContent());
        post.setImageUrl(request.getImageUrl());
        post.setAuthor(author);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
        return toDto(post);
    }

    @Transactional
    public PostDto editPost(Long postId, Long authorId, EditPostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found."));
        if (!post.getAuthor().getId().equals(authorId)) {
            throw new IllegalArgumentException("Unauthorized.");
        }
        post.setContent(request.getContent());
        post.setImageUrl(request.getImageUrl());
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
        return toDto(post);
    }

    @Transactional
    public void deletePost(Long postId, Long authorId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found."));
        if (!post.getAuthor().getId().equals(authorId)) {
            throw new IllegalArgumentException("Unauthorized.");
        }
        postRepository.delete(post);
    }

    private PostDto toDto(Post post) {
        int likeCount = likeRepository.countByPost(post);
        int commentCount = commentRepository.countByPost(post);
        List<CommentDto> comments = commentRepository.findByPost(post).stream()
                .map(comment -> new CommentDto(
                        comment.getId(),
                        comment.getContent(),
                        comment.getCreatedAt(),
                        comment.getAuthor().getId(),
                        comment.getAuthor().getUsername()
                ))
                .collect(Collectors.toList());
        return new PostDto(
                post.getId(),
                post.getContent(),
                post.getImageUrl(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getAuthor().getId(),
                post.getAuthor().getUsername(),
                likeCount,
                commentCount,
                comments
        );
    }
}
