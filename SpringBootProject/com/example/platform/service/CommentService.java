package com.example.platform.service;

import com.example.platform.dto.CommentDto;
import com.example.platform.dto.CommentRequest;
import com.example.platform.entity.Comment;
import com.example.platform.entity.Post;
import com.example.platform.entity.User;
import com.example.platform.repository.CommentRepository;
import com.example.platform.repository.PostRepository;
import com.example.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    public List<CommentDto> getCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found."));
        return commentRepository.findByPost(post).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto addComment(Long postId, Long authorId, CommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found."));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment);
        return toDto(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long authorId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found."));
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new IllegalArgumentException("Unauthorized.");
        }
        commentRepository.delete(comment);
    }

    private CommentDto toDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getAuthor().getId(),
                comment.getAuthor().getUsername()
        );
    }
}
