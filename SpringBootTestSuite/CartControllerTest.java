package SpringBootTestSuite;

import com.example.customermanagement.controller.CartController;
import com.example.customermanagement.entity.Cart;
import com.example.customermanagement.entity.CartItem;
import com.example.customermanagement.entity.Product;
import com.example.customermanagement.exception.ResourceNotFoundException;
import com.example.customermanagement.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for CartController covering add, get, and clear cart endpoints.
 */
@WebMvcTest(CartController.class)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        Mockito.reset(cartService);
    }

    @Test
    public void testAddToCart_WithValidData_ShouldReturnOk() throws Exception {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        when(cartService.addToCart(eq(1L), eq(1L), eq(2))).thenReturn(cart);

        mockMvc.perform(post("/api/cart/add")
                .param("userId", "1")
                .param("productId", "1")
                .param("quantity", "2"))
                .andExpect(status().isOk());
    }

    @Test
    public void testAddToCart_WithOutOfStock_ShouldReturnBadRequest() throws Exception {
        when(cartService.addToCart(eq(1L), eq(1L), eq(10))).thenThrow(new IllegalStateException("Out of stock"));

        mockMvc.perform(post("/api/cart/add")
                .param("userId", "1")
                .param("productId", "1")
                .param("quantity", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddToCart_WithInvalidUser_ShouldReturnNotFound() throws Exception {
        when(cartService.addToCart(eq(99L), eq(1L), eq(1))).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(post("/api/cart/add")
                .param("userId", "99")
                .param("productId", "1")
                .param("quantity", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetCart_WithExistingCart_ShouldReturnOk() throws Exception {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        when(cartService.getCartByUser(1L)).thenReturn(cart);

        mockMvc.perform(get("/api/cart/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetCart_WithNoCart_ShouldReturnOk() throws Exception {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        when(cartService.getCartByUser(2L)).thenReturn(cart);

        mockMvc.perform(get("/api/cart/2"))
                .andExpect(status().isOk());
    }

    @Test
    public void testClearCart_ShouldReturnOk() throws Exception {
        doNothing().when(cartService).clearCart(1L);

        mockMvc.perform(post("/api/cart/1/clear"))
                .andExpect(status().isOk());
    }
}
