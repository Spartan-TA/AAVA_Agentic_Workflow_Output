package com.example.platform.service;

import com.example.platform.dto.AnalyticsDto;
import com.example.platform.repository.UserRepository;
import com.example.platform.repository.PostRepository;
import com.example.platform.repository.CommentRepository;
import com.example.platform.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class AnalyticsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private LikeRepository likeRepository;

    public AnalyticsDto getAnalytics() {
        int totalUsers = (int) userRepository.count();
        int totalPosts = (int) postRepository.count();
        int totalComments = (int) commentRepository.count();
        int totalLikes = (int) likeRepository.count();
        Map<String, Integer> postsPerDay = new HashMap<>();
        Map<String, Integer> registrationsPerDay = new HashMap<>();
        // For demonstration, fill with dummy data
        for (int i = 6; i >= 0; i--) {
            String day = LocalDate.now().minusDays(i).toString();
            postsPerDay.put(day, (int) (Math.random() * 10));
            registrationsPerDay.put(day, (int) (Math.random() * 5));
        }
        return new AnalyticsDto(totalUsers, totalPosts, totalComments, totalLikes, postsPerDay, registrationsPerDay);
    }
}
