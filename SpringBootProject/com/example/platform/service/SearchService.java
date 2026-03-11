package com.example.platform.service;

import com.example.platform.dto.SearchResultDto;
import com.example.platform.dto.UserProfileDto;
import com.example.platform.dto.PostDto;
import com.example.platform.entity.User;
import com.example.platform.entity.Post;
import com.example.platform.repository.UserRepository;
import com.example.platform.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;

    public SearchResultDto search(String query) {
        List<UserProfileDto> users = userRepository.findByUsernameContainingIgnoreCase(query).stream()
                .map(user -> new UserProfileDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getBio(),
                        user.getAvatarUrl(),
                        user.getRole().name(),
                        user.isVerified()
                ))
                .collect(Collectors.toList());
        List<PostDto> posts = postRepository.findByContentContainingIgnoreCase(query).stream()
                .map(post -> new PostDto(
                        post.getId(),
                        post.getContent(),
                        post.getImageUrl(),
                        post.getCreatedAt(),
                        post.getUpdatedAt(),
                        post.getAuthor().getId(),
                        post.getAuthor().getUsername(),
                        0,
                        0,
                        null
                ))
                .collect(Collectors.toList());
        return new SearchResultDto(users, posts);
    }
}
