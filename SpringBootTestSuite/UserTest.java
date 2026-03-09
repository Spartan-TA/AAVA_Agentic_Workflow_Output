package com.company.wems.security.entity;

import org.junit.jupiter.api.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("admin")
                .password("password")
                .email("admin@company.com")
                .roles(new HashSet<>(Arrays.asList("ROLE_ADMIN", "ROLE_USER")))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create user with valid data")
    void testCreateUser_ValidData_Success() {
        assertAll(
                () -> assertNotNull(user),
                () -> assertEquals("admin", user.getUsername()),
                () -> assertEquals("password", user.getPassword()),
                () -> assertEquals("admin@company.com", user.getEmail()),
                () -> assertTrue(user.isEnabled()),
                () -> assertTrue(user.isAccountNonExpired()),
                () -> assertTrue(user.isAccountNonLocked()),
                () -> assertTrue(user.isCredentialsNonExpired())
        );
    }

    @Test
    @DisplayName("Should return authorities for multiple roles")
    void testGetAuthorities_MultipleRoles_Success() {
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertEquals(2, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("Should return empty authorities when roles is null")
    void testGetAuthorities_NullRoles_Empty() {
        User u = User.builder()
                .username("user2")
                .password("pass2")
                .email("user2@company.com")
                .roles(null)
                .build();
        assertNotNull(u.getAuthorities());
        assertEquals(0, u.getAuthorities().size());
    }

    @Test
    @DisplayName("Should return empty authorities when roles is empty")
    void testGetAuthorities_EmptyRoles_Empty() {
        User u = User.builder()
                .username("user3")
                .password("pass3")
                .email("user3@company.com")
                .roles(new HashSet<>())
                .build();
        assertNotNull(u.getAuthorities());
        assertEquals(0, u.getAuthorities().size());
    }

    @Test
    @DisplayName("Should return correct enabled/expired/locked flags")
    void testUserFlags() {
        User u = User.builder()
                .username("user4")
                .password("pass4")
                .email("user4@company.com")
                .roles(Set.of("ROLE_USER"))
                .enabled(false)
                .accountNonExpired(false)
                .accountNonLocked(false)
                .credentialsNonExpired(false)
                .build();
        assertFalse(u.isEnabled());
        assertFalse(u.isAccountNonExpired());
        assertFalse(u.isAccountNonLocked());
        assertFalse(u.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("Should allow null username/password/email (edge case)")
    void testNullUsernamePasswordEmail() {
        User u = User.builder()
                .username(null)
                .password(null)
                .email(null)
                .roles(Set.of("ROLE_USER"))
                .build();
        assertNull(u.getUsername());
        assertNull(u.getPassword());
        assertNull(u.getEmail());
    }
}
