package SpringBootTestSuite;

import com.example.customermanagement.entity.Cart;
import com.example.customermanagement.entity.CartItem;
import com.example.customermanagement.entity.Order;
import com.example.customermanagement.entity.Product;
import com.example.customermanagement.entity.User;
import com.example.customermanagement.enums.OrderStatus;
import com.example.customermanagement.exception.ResourceNotFoundException;
import com.example.customermanagement.repository.CartRepository;
import com.example.customermanagement.repository.OrderRepository;
import com.example.customermanagement.repository.UserRepository;
import com.example.customermanagement.service.NotificationService;
import com.example.customermanagement.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for OrderService covering order creation, status updates, and retrieval.
 */
@SpringBootTest
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

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
    public void testCreateOrder_FromCartWithItems_ShouldCreateOrder() {
        // Arrange
        User user = new User(); user.setId(1L);
        Product product = new Product(1L, "Product1", 10, 100.0);
        CartItem item = new CartItem(product, 2);
        Cart cart = new Cart(); cart.setUser(user); cart.setItems(new ArrayList<>());
        cart.getItems().add(item);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order order = orderService.createOrder(1L);

        // Assert
        assertNotNull(order);
        assertEquals(1, order.getItems().size());
        verify(notificationService, times(1)).sendOrderStatusNotification(any(Order.class));
    }

    @Test
    public void testCreateOrder_FromEmptyCart_ShouldThrowException() {
        // Arrange
        User user = new User(); user.setId(1L);
        Cart cart = new Cart(); cart.setUser(user); cart.setItems(new ArrayList<>());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> orderService.createOrder(1L));
    }

    @Test
    public void testUpdateOrderStatus_ToShippedWithTracking_ShouldUpdateStatus() {
        // Arrange
        Order order = new Order(); order.setId(1L); order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        orderService.updateOrderStatus(1L, OrderStatus.SHIPPED, "TRACK123");

        // Assert
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        assertEquals("TRACK123", order.getTrackingNumber());
        verify(notificationService, times(1)).sendOrderStatusNotification(order);
    }

    @Test
    public void testUpdateOrderStatus_WithoutTrackingNumber_ShouldNotSetTracking() {
        // Arrange
        Order order = new Order(); order.setId(2L); order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        orderService.updateOrderStatus(2L, OrderStatus.PROCESSING, null);

        // Assert
        assertEquals(OrderStatus.PROCESSING, order.getStatus());
        assertNull(order.getTrackingNumber());
        verify(notificationService, times(1)).sendOrderStatusNotification(order);
    }

    @Test
    public void testGetOrderById_WithValidId_ShouldReturnOrder() {
        // Arrange
        Order order = new Order(); order.setId(10L);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        // Act
        Order result = orderService.getOrderById(10L);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    @Test
    public void testGetOrderById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(99L));
    }

    @Test
    public void testGetOrdersByUser_WithOrders_ShouldReturnList() {
        // Arrange
        User user = new User(); user.setId(1L);
        Order order1 = new Order(); order1.setId(1L); order1.setUser(user);
        Order order2 = new Order(); order2.setId(2L); order2.setUser(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUser(user)).thenReturn(java.util.Arrays.asList(order1, order2));

        // Act
        java.util.List<Order> orders = orderService.getOrdersByUser(1L);

        // Assert
        assertNotNull(orders);
        assertEquals(2, orders.size());
    }

    @Test
    public void testGetOrdersByUser_WithNoOrders_ShouldReturnEmptyList() {
        // Arrange
        User user = new User(); user.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUser(user)).thenReturn(new ArrayList<>());

        // Act
        java.util.List<Order> orders = orderService.getOrdersByUser(2L);

        // Assert
        assertNotNull(orders);
        assertTrue(orders.isEmpty());
    }

    @Test
    public void testUpdateOrderStatus_WithInvalidOrderId_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.updateOrderStatus(99L, OrderStatus.SHIPPED, "TRACK999"));
    }
}
