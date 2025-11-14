import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.application.domain.User;
import com.application.dto.request.UserRequestDTO;
import com.application.dto.response.UserResponseDTO;
import com.application.exception.DuplicateResourceException;
import com.application.exception.ResourceNotFoundException;
import com.application.mapper.UserMapper;
import com.application.repository.UserRepository;
import com.application.service.UserService;
import com.application.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateUser_Success() {
        // Arrange
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setUsername("testuser");
        requestDTO.setEmail("testuser@example.com");
        requestDTO.setPassword("password");

        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("testuser@example.com")).thenReturn(false);
        when(userMapper.toEntity(requestDTO)).thenReturn(user);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(new UserResponseDTO());

        // Act
        UserResponseDTO responseDTO = userService.createUser(requestDTO);

        // Assert
        assertNotNull(responseDTO);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testCreateUser_UsernameAlreadyExists() {
        // Arrange
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setUsername("testuser");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> userService.createUser(requestDTO));
    }

    @Test
    public void testGetUserById_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDTO(user)).thenReturn(new UserResponseDTO());

        // Act
        UserResponseDTO responseDTO = userService.getUserById(1L);

        // Assert
        assertNotNull(responseDTO);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetUserById_NotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));
    }
}