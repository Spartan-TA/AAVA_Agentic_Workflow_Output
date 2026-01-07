package com.example.customermanagement.controller;

import com.example.customermanagement.dto.CartItemDTO;
import com.example.customermanagement.model.Cart;
import com.example.customermanagement.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for cart operations: add, view, clear cart.
 */
@RestController
@RequestMapping("/api/cart")
@Validated
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * Add item to cart.
     * @param cartItemDTO Cart item data
     * @param authentication Authenticated user
     * @return Updated cart
     */
    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(@Valid @RequestBody CartItemDTO cartItemDTO, Authentication authentication) {
        String username = authentication.getName();
        Cart cart = cartService.addToCart(username, cartItemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }

    /**
     * View current user's cart.
     * @param authentication Authenticated user
     * @return Cart
     */
    @GetMapping
    public ResponseEntity<Cart> viewCart(Authentication authentication) {
        String username = authentication.getName();
        Cart cart = cartService.getCartByUsername(username);
        return ResponseEntity.ok(cart);
    }

    /**
     * Clear current user's cart.
     * @param authentication Authenticated user
     * @return Success message
     */
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(Authentication authentication) {
        String username = authentication.getName();
        cartService.clearCart(username);
        return ResponseEntity.ok("Cart cleared successfully.");
    }
}
