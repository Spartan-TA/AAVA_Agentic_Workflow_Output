package SpringBootTestSuite;

import com.example.customermanagement.controller.ProductController;
import com.example.customermanagement.entity.Product;
import com.example.customermanagement.exception.ResourceNotFoundException;
import com.example.customermanagement.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for ProductController covering product retrieval and in-stock filtering.
 */
@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        Mockito.reset(productService);
    }

    @Test
    public void testGetAllProducts_ShouldReturnProductList() throws Exception {
        Product p1 = new Product(1L, "Product1", 10, 100.0);
        Product p2 = new Product(2L, "Product2", 5, 50.0);
        when(productService.getAllProducts()).thenReturn(Arrays.asList(p1, p2));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Product1"))
                .andExpect(jsonPath("$[1].name").value("Product2"));
    }

    @Test
    public void testGetAllProducts_WhenNoProducts_ShouldReturnEmptyList() throws Exception {
        when(productService.getAllProducts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void testGetProductById_WithValidId_ShouldReturnProduct() throws Exception {
        Product product = new Product(1L, "Product1", 10, 100.0);
        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Product1"));
    }

    @Test
    public void testGetProductById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        when(productService.getProductById(99L)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetInStockProducts_ShouldReturnOnlyInStock() throws Exception {
        Product p1 = new Product(1L, "Product1", 10, 100.0);
        Product p2 = new Product(2L, "Product2", 0, 50.0);
        when(productService.getAllProducts()).thenReturn(Arrays.asList(p1, p2));

        mockMvc.perform(get("/api/products/in-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Product1"))
                .andExpect(jsonPath("$[0].stock").value(10));
    }
}
