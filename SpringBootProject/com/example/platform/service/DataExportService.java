package com.example.platform.service;

import com.example.platform.entity.User;
import com.example.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataExportService {
    @Autowired
    private UserRepository userRepository;

    public String exportAllUsersToCsv() {
        List<User> users = userRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("id,username,email,role,verified
");
        for (User user : users) {
            sb.append(user.getId()).append(",")
              .append(user.getUsername()).append(",")
              .append(user.getEmail()).append(",")
              .append(user.getRole().name()).append(",")
              .append(user.isVerified()).append("
");
        }
        return sb.toString();
    }
}
