package com.warehouseems.security.service;

import com.warehouseems.security.entity.ApiKey;
import com.warehouseems.security.repository.ApiKeyRepository;
import com.warehouseems.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for API key validation and generation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Transactional
    public UserDetails validateApiKey(String apiKey) {
        Optional<ApiKey> apiKeyOpt = apiKeyRepository.findByKeyAndEnabledTrue(apiKey);
        if (apiKeyOpt.isEmpty()) return null;
        ApiKey key = apiKeyOpt.get();
        if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        key.setLastUsedAt(LocalDateTime.now());
        apiKeyRepository.save(key);
        return userDetailsService.loadUserByUsername(key.getUsername());
    }

    @Transactional
    public ApiKey generateApiKey(String username, LocalDateTime expiresAt) {
        if (userRepository.findByUsername(username).isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        ApiKey apiKey = ApiKey.builder()
                .key(UUID.randomUUID().toString())
                .username(username)
                .expiresAt(expiresAt)
                .enabled(true)
                .build();
        return apiKeyRepository.save(apiKey);
    }
}
