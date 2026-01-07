package com.example.usermanagement.service;

import com.example.usermanagement.dto.*;
import com.example.usermanagement.entity.*;
import com.example.usermanagement.exception.*;
import com.example.usermanagement.repository.*;
import com.example.usermanagement.util.EmailTemplateUtil;
import com.example.usermanagement.util.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service class for user-related business logic.
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final ProfileAuditRepository profileAuditRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaService captchaService;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       VerificationTokenRepository verificationTokenRepository,
                       ProfileAuditRepository profileAuditRepository,
                       EmailService emailService,
                       PasswordEncoder passwordEncoder,
                       CaptchaService captchaService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.profileAuditRepository = profileAuditRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.captchaService = captchaService;
    }

    /**
     * Registers a new user and sends a verification email.
     * @param request RegisterRequest DTO
     * @return UserDto
     */
    @Transactional
    public UserDto registerUser(RegisterRequest request) {
        // Validate captcha
        if (!captchaService.verify(request.getCaptcha())) {
            throw new InvalidTokenException("Invalid captcha");
        }
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already in use");
        }
        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);
        user.setLocked(false);
        user.setCreatedAt(LocalDateTime.now());
        // Assign default role
        Role role = roleRepository.findByName("USER");
        user.setRoles(Collections.singleton(role));
        userRepository.save(user);
        // Create verification token
        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusDays(1));
        verificationTokenRepository.save(token);
        // Send verification email
        emailService.sendVerification(user, token.getToken());
        return UserDto.fromEntity(user);
    }

    /**
     * Confirms user's email using the verification token.
     * @param token Verification token
     */
    @Transactional
    public void confirmEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Token expired");
        }
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
    }

    /**
     * Updates the user's profile and audits the changes.
     * @param userId User ID
     * @param dto ProfileUpdateDto
     * @return UserDto
     */
    @Transactional
    public UserDto updateProfile(Long userId, ProfileUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        // Audit old values
        ProfileAudit audit = new ProfileAudit();
        audit.setUser(user);
        audit.setChangedAt(LocalDateTime.now());
        audit.setOldFirstName(user.getFirstName());
        audit.setOldLastName(user.getLastName());
        audit.setOldEmail(user.getEmail());
        // Update fields
        if (StringUtils.hasText(dto.getFirstName())) user.setFirstName(dto.getFirstName());
        if (StringUtils.hasText(dto.getLastName())) user.setLastName(dto.getLastName());
        if (StringUtils.hasText(dto.getEmail()) && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new DuplicateEmailException("Email already in use");
            }
            audit.setOldEmail(user.getEmail());
            user.setEmail(dto.getEmail());
        }
        userRepository.save(user);
        profileAuditRepository.save(audit);
        return UserDto.fromEntity(user);
    }

    /**
     * Searches users by keyword (email, first name, last name) with pagination.
     * @param keyword search keyword
     * @param page page number
     * @param size page size
     * @return Page of UserDto
     */
    public Page<UserDto> searchUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userRepository.search(keyword, pageable);
        return users.map(UserDto::fromEntity);
    }

    /**
     * Finds a user by ID.
     * @param userId User ID
     * @return User entity
     */
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
