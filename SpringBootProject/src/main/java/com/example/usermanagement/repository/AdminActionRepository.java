package com.example.usermanagement.repository;

import com.example.usermanagement.domain.AdminAction;
import com.example.usermanagement.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdminActionRepository extends JpaRepository<AdminAction, Long> {
    List<AdminAction> findByAdmin(User admin);
    List<AdminAction> findByTargetUser(User targetUser);
}
