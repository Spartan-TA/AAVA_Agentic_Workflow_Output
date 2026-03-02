package com.example.usermanagement.repository;

import com.example.usermanagement.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.enabled = true")
    Iterable<User> findAllEnabled();

    @Query("SELECT u FROM User u WHERE u.accountNonLocked = false")
    Iterable<User> findAllLocked();
}
