package com.example.platform.service;

import com.example.platform.dto.DashboardDto;
import com.example.platform.entity.User;
import com.example.platform.repository.PostRepository;
import com.example.platform.repository.CommentRepository;
import com.example.platform.repository.LikeRepository;
import com.example.platform.repository.UserRepository;
import com.example.platform.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    public DashboardDto getDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        int postCount = postRepository.countByAuthor(user);
        int commentCount = commentRepository.countByAuthor(user);
        int likeCount = likeRepository.findByPost_Author(user).size();
        int followerCount = 0; // Implement follower logic if needed
        int followingCount = 0; // Implement following logic if needed
        int notificationCount = (int) notificationRepository.countByRecipientAndReadFalse(user);
        return new DashboardDto(postCount, commentCount, likeCount, followerCount, followingCount, notificationCount);
    }
}
