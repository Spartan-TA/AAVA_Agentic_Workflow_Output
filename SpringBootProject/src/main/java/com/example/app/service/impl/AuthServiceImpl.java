package com.example.app.service.impl;

import com.example.app.dto.LoginDto;
import com.example.app.dto.JwtResponseDto;
import com.example.app.dto.MfaDto;
import com.example.app.entity.User;
import com.example.app.entity.VerificationToken;
import com.example.app.repository.UserRepository;
import com.example.app.repository.VerificationTokenRepository;
import com.example.app.service.AuthService;
import com.example.app.security.JwtTokenProvider;
import com.example.app.exception.RegistrationException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Override
    public JwtResponseDto authenticateUser(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
        User user = userRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new RegistrationException("User not found"));
        String token = jwtTokenProvider.generateToken(authentication);
        return new JwtResponseDto(token, user.getUsername(), user.getEmail());
    }

    @Override
    public void initiateMfa(String username) {
        // Implement MFA logic (e.g., send code via email or SMS)
    }

    @Override
    public boolean verifyMfa(String username, MfaDto mfaDto) {
        // Implement MFA verification logic
        return true;
    }

    @Override
    @Transactional
    public void sendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RegistrationException("User not found"));
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();
        tokenRepository.save(verificationToken);
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Email Verification");
        mailMessage.setText("To verify your account, please click here: " +
                "http://localhost:8080/api/auth/verify?token=" + token);
        mailSender.send(mailMessage);
    }

    @Override
    @Transactional
    public boolean verifyToken(String token) {
        Optional<VerificationToken> verificationToken = tokenRepository.findByToken(token);
        if (verificationToken.isPresent() && verificationToken.get().getExpiryDate().isAfter(LocalDateTime.now())) {
            User user = verificationToken.get().getUser();
            user.setEnabled(true);
            userRepository.save(user);
            tokenRepository.delete(verificationToken.get());
            return true;
        }
        return false;
    }

    @Override
    public void logout(String username) {
        // JWT is stateless; implement blacklist if needed
    }
}
