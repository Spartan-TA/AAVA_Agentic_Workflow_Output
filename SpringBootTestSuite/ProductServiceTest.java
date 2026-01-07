package SpringBootTestSuite;

import com.example.customermanagement.entity.Product;
import com.example.customermanagement.exception.ResourceNotFoundException;
import com.example.customermanagement.repository.ProductRepository;
import com.example.customermanagement.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for ProductService covering product retrieval, availability, and stock updates.
 */
@SpringBootTest
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

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
    public void testGetAllProducts_ShouldReturnProductList() {
        // Arrange
        Product p1 = new Product(1L, "Product1", 10, 100.0);
        Product p2 = new Product(2L, "Product2", 5, 50.0);
        when(productRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        // Act
        List<Product> products = productService.getAllProducts();

        // Assert
        assertNotNull(products);
        assertEquals(2, products.size());
    }

    @Test
    public void testGetAllProducts_WhenNoProducts_ShouldReturnEmptyList() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Product> products = productService.getAllProducts();

        // Assert
        assertNotNull(products);
        assertTrue(products.isEmpty());
    }

    @Test
    public void testGetProductById_WithValidId_ShouldReturnProduct() {
        // Arrange
        Product product = new Product(1L, "Product1", 10, 100.0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        Product result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void testGetProductById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    public void testIsProductAvailable_WhenInStock_ShouldReturnTrue() {
        // Arrange
        Product product = new Product(1L, "Product1", 10, 100.0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        boolean available = productService.isProductAvailable(1L, 5);

        // Assert
        assertTrue(available);
    }

    @Test
    public void testIsProductAvailable_WhenOutOfStock_ShouldReturnFalse() {
        // Arrange
        Product product = new Product(1L, "Product1", 0, 100.0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        boolean available = productService.isProductAvailable(1L, 1);

        // Assert
        assertFalse(available);
    }

    @Test
    public void testIsProductAvailable_WhenInsufficientStock_ShouldReturnFalse() {
        // Arrange
        Product product = new Product(1L, "Product1", 2, 100.0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        boolean available = productService.isProductAvailable(1L, 5);

        // Assert
        assertFalse(available);
    }

    @Test
    public void testUpdateStock_WithValidQuantity_ShouldUpdateStock() {
        // Arrange
        Product product = new Product(1L, "Product1", 10, 100.0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        productService.updateStock(1L, 5);

        // Assert
        assertEquals(5, product.getStock());
    }

    @Test
    public void testUpdateStock_WithInsufficientStock_ShouldThrowException() {
        // Arrange
        Product product = new Product(1L, "Product1", 3, 100.0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> productService.updateStock(1L, 5));
    }

    @Test
    public void testUpdateStock_WithInvalidProductId_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.updateStock(99L, 1));
    }
}
