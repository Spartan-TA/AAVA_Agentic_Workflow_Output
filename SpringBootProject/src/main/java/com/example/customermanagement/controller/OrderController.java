package com.example.customermanagement.controller;

import com.example.customermanagement.dto.OrderDTO;
import com.example.customermanagement.model.Order;
import com.example.customermanagement.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for order operations and admin status update.
 */
@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Create a new order from the user's cart.
     * @param orderDTO Order data
     * @param authentication Authenticated user
     * @return Created order
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderDTO orderDTO, Authentication authentication) {
        String username = authentication.getName();
        Order order = orderService.createOrder(username, orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * View all orders for the authenticated user.
     * @param authentication Authenticated user
     * @return List of orders
     */
    @GetMapping
    public ResponseEntity<List<Order>> getOrders(Authentication authentication) {
        String username = authentication.getName();
        List<Order> orders = orderService.getOrdersByUsername(username);
        return ResponseEntity.ok(orders);
    }

    /**
     * Update order status (admin only).
     * @param orderId Order ID
     * @param status New status
     * @return Updated order
     */
    @PutMapping("/update-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> updateOrderStatus(@RequestParam Long orderId, @RequestParam String status) {
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }
}
