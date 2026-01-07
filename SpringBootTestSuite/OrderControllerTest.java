package SpringBootTestSuite;

import com.example.customermanagement.controller.OrderController;
import com.example.customermanagement.entity.Order;
import com.example.customermanagement.enums.OrderStatus;
import com.example.customermanagement.exception.ResourceNotFoundException;
import com.example.customermanagement.service.OrderService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for OrderController covering order creation, retrieval, and status updates.
 */
@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        Mockito.reset(orderService);
    }

    @Test
    public void testCreateOrder_WithValidUser_ShouldReturnCreated() throws Exception {
        Order order = new Order(); order.setId(1L);
        when(orderService.createOrder(1L)).thenReturn(order);

        mockMvc.perform(post("/api/orders/create")
                .param("userId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testCreateOrder_WithEmptyCart_ShouldReturnBadRequest() throws Exception {
        when(orderService.createOrder(1L)).thenThrow(new IllegalStateException("Empty cart"));

        mockMvc.perform(post("/api/orders/create")
                .param("userId", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetOrderById_WithValidId_ShouldReturnOk() throws Exception {
        Order order = new Order(); order.setId(1L);
        when(orderService.getOrderById(1L)).thenReturn(order);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testGetOrderById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        when(orderService.getOrderById(99L)).thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetUserOrders_WithOrders_ShouldReturnList() throws Exception {
        Order order1 = new Order(); order1.setId(1L);
        Order order2 = new Order(); order2.setId(2L);
        when(orderService.getOrdersByUser(1L)).thenReturn(Arrays.asList(order1, order2));

        mockMvc.perform(get("/api/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    public void testGetUserOrders_WithNoOrders_ShouldReturnEmptyList() throws Exception {
        when(orderService.getOrdersByUser(2L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/orders/user/2"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void testUpdateOrderStatus_WithValidData_ShouldReturnOk() throws Exception {
        Order order = new Order(); order.setId(1L); order.setStatus(OrderStatus.SHIPPED);
        when(orderService.updateOrderStatus(1L, OrderStatus.SHIPPED, "TRACK123")).thenReturn(order);

        mockMvc.perform(put("/api/orders/1/status")
                .param("status", "SHIPPED")
                .param("trackingNumber", "TRACK123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    public void testUpdateOrderStatus_WithInvalidOrderId_ShouldReturnNotFound() throws Exception {
        when(orderService.updateOrderStatus(99L, OrderStatus.SHIPPED, "TRACK999")).thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(put("/api/orders/99/status")
                .param("status", "SHIPPED")
                .param("trackingNumber", "TRACK999"))
                .andExpect(status().isNotFound());
    }
}
