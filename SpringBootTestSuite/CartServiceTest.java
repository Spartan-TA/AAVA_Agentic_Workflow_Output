package SpringBootTestSuite;

import com.example.customermanagement.entity.Cart;
import com.example.customermanagement.entity.CartItem;
import com.example.customermanagement.entity.Product;
import com.example.customermanagement.entity.User;
import com.example.customermanagement.exception.ResourceNotFoundException;
import com.example.customermanagement.repository.CartRepository;
import com.example.customermanagement.repository.ProductRepository;
import com.example.customermanagement.repository.UserRepository;
import com.example.customermanagement.service.CartService;
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
 * Comprehensive unit tests for CartService covering add, get, and clear cart scenarios.
 */
@SpringBootTest
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

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
    public void testAddToCart_WithAvailableProduct_ShouldAddItem() {
        // Arrange
        User user = new User(); user.setId(1L);
        Product product = new Product(1L, "Product1", 10, 100.0);
        Cart cart = new Cart(); cart.setUser(user); cart.setItems(new ArrayList<>());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // Act
        Cart result = cartService.addToCart(1L, 1L, 2);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(2, result.getItems().get(0).getQuantity());
    }

    @Test
    public void testAddToCart_WithOutOfStockProduct_ShouldThrowException() {
        // Arrange
        User user = new User(); user.setId(1L);
        Product product = new Product(1L, "Product1", 0, 100.0);
        Cart cart = new Cart(); cart.setUser(user); cart.setItems(new ArrayList<>());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> cartService.addToCart(1L, 1L, 1));
    }

    @Test
    public void testAddToCart_WithInsufficientStock_ShouldThrowException() {
        // Arrange
        User user = new User(); user.setId(1L);
        Product product = new Product(1L, "Product1", 2, 100.0);
        Cart cart = new Cart(); cart.setUser(user); cart.setItems(new ArrayList<>());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> cartService.addToCart(1L, 1L, 5));
    }

    @Test
    public void testAddToCart_ProductAlreadyInCart_ShouldUpdateQuantity() {
        // Arrange
        User user = new User(); user.setId(1L);
        Product product = new Product(1L, "Product1", 10, 100.0);
        CartItem item = new CartItem(product, 2);
        Cart cart = new Cart(); cart.setUser(user); cart.setItems(new ArrayList<>());
        cart.getItems().add(item);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // Act
        Cart result = cartService.addToCart(1L, 1L, 3);

        // Assert
        assertEquals(1, result.getItems().size());
        assertEquals(5, result.getItems().get(0).getQuantity());
    }

    @Test
    public void testGetCartByUser_WithExistingCart_ShouldReturnCart() {
        // Arrange
        User user = new User(); user.setId(1L);
        Cart cart = new Cart(); cart.setUser(user); cart.setItems(new ArrayList<>());
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        // Act
        Cart result = cartService.getCartByUser(1L);

        // Assert
        assertNotNull(result);
        assertEquals(user, result.getUser());
    }

    @Test
    public void testGetCartByUser_WithNoCart_ShouldReturnEmptyCart() {
        // Arrange
        when(cartRepository.findByUserId(2L)).thenReturn(Optional.empty());

        // Act
        Cart result = cartService.getCartByUser(2L);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getItems().size());
    }

    @Test
    public void testClearCart_ShouldRemoveAllItems() {
        // Arrange
        User user = new User(); user.setId(1L);
        CartItem item = new CartItem(new Product(1L, "Product1", 10, 100.0), 2);
        Cart cart = new Cart(); cart.setUser(user); cart.setItems(new ArrayList<>());
        cart.getItems().add(item);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // Act
        cartService.clearCart(1L);

        // Assert
        assertEquals(0, cart.getItems().size());
    }

    @Test
    public void testAddToCart_WithInvalidUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> cartService.addToCart(99L, 1L, 1));
    }

    @Test
    public void testAddToCart_WithInvalidProduct_ShouldThrowException() {
        // Arrange
        User user = new User(); user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> cartService.addToCart(1L, 99L, 1));
    }
}
