package SpringBootTestSuite;

import com.example.customermanagement.entity.User;
import com.example.customermanagement.enums.Role;
import com.example.customermanagement.exception.ResourceNotFoundException;
import com.example.customermanagement.repository.OrderRepository;
import com.example.customermanagement.repository.UserRepository;
import com.example.customermanagement.service.ReportService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReportService covering sales report generation and admin validation.
 */
@SpringBootTest
public class ReportServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportService reportService;

    private AutoCloseable closeable;

    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testGenerateSalesReport_WithAdminUser_ShouldReturnReport() {
        // Arrange
        User admin = new User();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(orderRepository.findAll()).thenReturn(java.util.Collections.emptyList());

        // Act
        String report = reportService.generateSalesReport(1L);

        // Assert
        assertNotNull(report);
        assertTrue(report.contains("Sales Report"));
    }

    @Test
    public void testGenerateSalesReport_WithNonAdminUser_ShouldThrowException() {
        // Arrange
        User user = new User();
        user.setId(2L);
        user.setRole(Role.CUSTOMER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(SecurityException.class, () -> reportService.generateSalesReport(2L));
    }

    @Test
    public void testGenerateSalesReport_WithInvalidUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> reportService.generateSalesReport(99L));
    }

    @Test
    public void testGenerateSalesReport_WithNoOrders_ShouldReturnEmptyReport() {
        // Arrange
        User admin = new User();
        admin.setId(3L);
        admin.setRole(Role.ADMIN);
        when(userRepository.findById(3L)).thenReturn(Optional.of(admin));
        when(orderRepository.findAll()).thenReturn(java.util.Collections.emptyList());

        // Act
        String report = reportService.generateSalesReport(3L);

        // Assert
        assertNotNull(report);
        assertTrue(report.contains("Sales Report"));
    }

    @Test
    public void testGenerateSalesReport_WithNullUserId_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> reportService.generateSalesReport(null));
    }
}
