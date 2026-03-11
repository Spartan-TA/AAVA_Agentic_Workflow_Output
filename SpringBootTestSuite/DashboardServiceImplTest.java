package SpringBootTestSuite;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.DashboardServiceImpl;
import com.example.demo.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class DashboardServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("dashboarduser");
        user.setEmail("dashboard@example.com");
    }

    @Test
    void testGetDashboardData_HappyPath() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User dashboardData = dashboardService.getDashboardData(1L);
        assertEquals("dashboarduser", dashboardData.getUsername());
    }

    @Test
    void testGetDashboardData_NotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> dashboardService.getDashboardData(2L));
    }

    @Test
    void testGetAllUsers_HappyPath() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));
        List<User> users = dashboardService.getAllUsers();
        assertEquals(1, users.size());
    }

    @Test
    void testGetAllUsers_EmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        List<User> users = dashboardService.getAllUsers();
        assertTrue(users.isEmpty());
    }

    @Test
    void testGetDashboardData_NullId() {
        assertThrows(IllegalArgumentException.class, () -> dashboardService.getDashboardData(null));
    }

    @Test
    void testGetAllUsers_Exception() {
        when(userRepository.findAll()).thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class, () -> dashboardService.getAllUsers());
    }
}