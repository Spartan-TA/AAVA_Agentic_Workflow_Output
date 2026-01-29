package com.warehouse.ems.security;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.*;
import java.util.*;

import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_ValidInput_Success() {
        RegisterRequestDto dto = new RegisterRequestDto("user1", "pass", "user1@wh.com", Set.of("WORKER"));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        UserEntity user = userService.registerUser(dto);
        Assertions.assertEquals("user1", user.getUsername());
        Assertions.assertEquals("encodedPass", user.getPassword());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void testRegisterUser_DuplicateUsername_ThrowsException() {
        RegisterRequestDto dto = new RegisterRequestDto("user1", "pass", "user1@wh.com", Set.of("WORKER"));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(new UserEntity()));
        Assertions.assertThrows(DuplicateUsernameException.class, () -> userService.registerUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLogin_ValidCredentials_ReturnsToken() {
        LoginRequestDto dto = new LoginRequestDto("user1", "pass");
        UserEntity user = new UserEntity(1L, "user1", "encodedPass", "user1@wh.com", Set.of("WORKER"), true, LocalDateTime.now());
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "encodedPass")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any())).thenReturn("jwt.token.value");
        String token = userService.login(dto);
        Assertions.assertNotNull(token);
        Assertions.assertEquals("jwt.token.value", token);
    }

    @Test
    void testLogin_InvalidCredentials_ThrowsException() {
        LoginRequestDto dto = new LoginRequestDto("user1", "wrongpass");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());
        Assertions.assertThrows(AuthenticationException.class, () -> userService.login(dto));
    }

    @Test
    void testLoadUserByUsername_ExistingUser_ReturnsUserDetails() {
        UserEntity user = new UserEntity(1L, "user1", "pass", "user1@wh.com", Set.of("WORKER"), true, LocalDateTime.now());
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        UserDetails details = userService.loadUserByUsername("user1");
        Assertions.assertEquals("user1", details.getUsername());
        Assertions.assertTrue(details.isEnabled());
    }

    @Test
    void testLoadUserByUsername_NotFound_ThrowsException() {
        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());
        Assertions.assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("nouser"));
    }

    @Test
    void testRegisterUser_NullUsername_ThrowsException() {
        RegisterRequestDto dto = new RegisterRequestDto(null, "pass", "user1@wh.com", Set.of("WORKER"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
    }

    @Test
    void testRegisterUser_EmptyPassword_ThrowsException() {
        RegisterRequestDto dto = new RegisterRequestDto("user1", "", "user1@wh.com", Set.of("WORKER"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
    }

    @Test
    void testRegisterUser_InvalidEmail_ThrowsException() {
        RegisterRequestDto dto = new RegisterRequestDto("user1", "pass", "invalidemail", Set.of("WORKER"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
    }

    @Test
    void testLogin_DisabledUser_ThrowsException() {
        LoginRequestDto dto = new LoginRequestDto("user1", "pass");
        UserEntity user = new UserEntity(1L, "user1", "encodedPass", "user1@wh.com", Set.of("WORKER"), false, LocalDateTime.now());
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        Assertions.assertThrows(DisabledException.class, () -> userService.login(dto));
    }

    @Test
    void testRegisterUser_WithMultipleRoles_Success() {
        RegisterRequestDto dto = new RegisterRequestDto("admin1", "pass", "admin1@wh.com", Set.of("ADMIN", "HR"));
        when(userRepository.findByUsername("admin1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        UserEntity user = userService.registerUser(dto);
        Assertions.assertEquals(2, user.getRoles().size());
        Assertions.assertTrue(user.getRoles().contains("ADMIN"));
        Assertions.assertTrue(user.getRoles().contains("HR"));
    }

    @Test
    void testLogin_WrongPassword_ThrowsException() {
        LoginRequestDto dto = new LoginRequestDto("user1", "wrongpass");
        UserEntity user = new UserEntity(1L, "user1", "encodedPass", "user1@wh.com", Set.of("WORKER"), true, LocalDateTime.now());
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "encodedPass")).thenReturn(false);
        Assertions.assertThrows(BadCredentialsException.class, () -> userService.login(dto));
    }

    @Test
    void testLoadUserByUsername_DisabledUser_ReturnsDisabledUserDetails() {
        UserEntity user = new UserEntity(1L, "user1", "pass", "user1@wh.com", Set.of("WORKER"), false, LocalDateTime.now());
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        UserDetails details = userService.loadUserByUsername("user1");
        Assertions.assertFalse(details.isEnabled());
    }

    @Test
    void testRegisterUser_NullRoles_ThrowsException() {
        RegisterRequestDto dto = new RegisterRequestDto("user1", "pass", "user1@wh.com", null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
    }

    @Test
    void testRegisterUser_EmptyRoles_ThrowsException() {
        RegisterRequestDto dto = new RegisterRequestDto("user1", "pass", "user1@wh.com", Set.of());
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
    }