package com.example.usermanagement.repository;

import com.example.usermanagement.domain.VerificationToken;
import com.example.usermanagement.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    void deleteByUser(User user);
}
