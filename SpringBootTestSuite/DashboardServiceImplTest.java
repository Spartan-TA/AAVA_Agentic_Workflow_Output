package com.example.usermanagement.service;

import com.example.usermanagement.entity.User;
import com.example.usermanagement.service.impl.DashboardServiceImpl;
import com.example.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for DashboardServiceImpl covering dashboard data retrieval and edge cases.
 */
public class DashboardServiceImplTest {
    @Mock private UserRepository userRepository;
    @InjectMocks private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDashboardData_ValidUser_Success() {
        User user = new User();
        user.setEmail("dash@example.com");
        when(userRepository.count()).thenReturn(10L);
        Map<String, Object> data = dashboardService.getDashboardData(user);
        assertNotNull(data);
        assertEquals(10L, data.get("userCount"));
    }

    @Test
    void testGetDashboardData_NullUser_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> dashboardService.getDashboardData(null));
    }
}
