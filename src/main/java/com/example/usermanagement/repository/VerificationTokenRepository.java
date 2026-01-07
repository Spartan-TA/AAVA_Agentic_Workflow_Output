package com.example.usermanagement.repository;

import com.example.usermanagement.entity.VerificationToken;
import com.example.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for VerificationToken entity.
 * Provides CRUD operations and custom queries for verification tokens.
 */
@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    /**
     * Find token by token string.
     * @param token Token string
     * @return Optional of VerificationToken
     */
    Optional<VerificationToken> findByToken(String token);

    /**
     * Find token by user.
     * @param user User entity
     * @return Optional of VerificationToken
     */
    Optional<VerificationToken> findByUser(User user);
}
