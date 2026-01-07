package SpringBootTestSuite;

import com.example.customermanagement.entity.Order;
import com.example.customermanagement.entity.User;
import com.example.customermanagement.enums.OrderStatus;
import com.example.customermanagement.service.EmailService;
import com.example.customermanagement.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService covering order status notifications.
 */
@SpringBootTest
public class NotificationServiceTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

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
    public void testSendOrderStatusNotification_WithValidOrder_ShouldSendEmail() {
        // Arrange
        User user = new User();
        user.setEmail("customer@example.com");
        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.SHIPPED);
        order.setTrackingNumber("TRACK123");

        // Act
        notificationService.sendOrderStatusNotification(order);

        // Assert
        verify(emailService, times(1)).sendEmail(eq("customer@example.com"), anyString(), contains("TRACK123"));
    }

    @Test
    public void testSendOrderStatusNotification_WithNullTrackingNumber_ShouldSendEmailWithoutTracking() {
        // Arrange
        User user = new User();
        user.setEmail("customer2@example.com");
        Order order = new Order();
        order.setId(2L);
        order.setUser(user);
        order.setStatus(OrderStatus.PROCESSING);
        order.setTrackingNumber(null);

        // Act
        notificationService.sendOrderStatusNotification(order);

        // Assert
        verify(emailService, times(1)).sendEmail(eq("customer2@example.com"), anyString(), not(contains("TRACK")));
    }

    @Test
    public void testSendOrderStatusNotification_WithNullUser_ShouldNotSendEmail() {
        // Arrange
        Order order = new Order();
        order.setId(3L);
        order.setUser(null);
        order.setStatus(OrderStatus.PENDING);

        // Act
        notificationService.sendOrderStatusNotification(order);

        // Assert
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testSendOrderStatusNotification_WithNullOrder_ShouldNotThrow() {
        // Act & Assert
        notificationService.sendOrderStatusNotification(null);
        // Should not throw any exception
    }

    @Test
    public void testSendOrderStatusNotification_WithNullEmail_ShouldNotSend() {
        // Arrange
        User user = new User();
        user.setEmail(null);
        Order order = new Order();
        order.setId(4L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        // Act
        notificationService.sendOrderStatusNotification(order);

        // Assert
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }
}
