package com.example.usermanagement.repository;

import com.example.usermanagement.domain.BackupCode;
import com.example.usermanagement.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BackupCodeRepository extends JpaRepository<BackupCode, Long> {
    List<BackupCode> findByUser(User user);
    void deleteByUser(User user);
}
