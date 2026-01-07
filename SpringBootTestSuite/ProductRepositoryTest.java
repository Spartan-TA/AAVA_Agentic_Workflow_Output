package SpringBootTestSuite;

import com.example.customermanagement.entity.Product;
import com.example.customermanagement.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ProductRepository covering CRUD and query methods.
 */
@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product product;

    @BeforeEach
    public void setup() {
        product = new Product();
        product.setName("RepoProduct");
        product.setStock(10);
        product.setPrice(99.99);
        productRepository.save(product);
    }

    @AfterEach
    public void tearDown() {
        productRepository.deleteAll();
    }

    @Test
    public void testFindById_ShouldReturnProduct() {
        Optional<Product> found = productRepository.findById(product.getId());
        assertTrue(found.isPresent());
        assertEquals("RepoProduct", found.get().getName());
    }

    @Test
    public void testFindAll_ShouldReturnList() {
        assertFalse(productRepository.findAll().isEmpty());
    }

    @Test
    public void testDeleteProduct_ShouldRemoveProduct() {
        productRepository.delete(product);
        Optional<Product> found = productRepository.findById(product.getId());
        assertFalse(found.isPresent());
    }

    @Test
    public void testSaveProduct_ShouldPersistProduct() {
        Product newProduct = new Product();
        newProduct.setName("NewProduct");
        newProduct.setStock(5);
        newProduct.setPrice(49.99);
        Product saved = productRepository.save(newProduct);
        assertNotNull(saved.getId());
        assertEquals("NewProduct", saved.getName());
    }
}
