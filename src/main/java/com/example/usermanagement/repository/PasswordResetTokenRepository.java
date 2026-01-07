package com.example.usermanagement.repository;

import com.example.usermanagement.entity.PasswordResetToken;
import com.example.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for PasswordResetToken entity.
 * Provides CRUD operations and custom queries for password reset tokens.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    /**
     * Find token by token string.
     * @param token Token string
     * @return Optional of PasswordResetToken
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Find token by user.
     * @param user User entity
     * @return Optional of PasswordResetToken
     */
    Optional<PasswordResetToken> findByUser(User user);
}
