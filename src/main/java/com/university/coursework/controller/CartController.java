package com.university.coursework.controller;

import com.university.coursework.domain.CartDTO;
import com.university.coursework.domain.CartItemDTO;
import com.university.coursework.domain.OrderDTO;
import com.university.coursework.entity.CartEntity;
import com.university.coursework.exception.CartNotFoundException;
import com.university.coursework.repository.CartRepository;
import com.university.coursework.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Tag(name = "Cart Management", description = "APIs for managing user carts")
public class CartController {

    private final CartService cartService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{cartId}")
    @Operation(summary = "Get cart by ID", description = "Retrieves the cart for a specific ID.")
    public ResponseEntity<CartDTO> getCartById(@PathVariable UUID cartId) {
        return ResponseEntity.ok(cartService.findById(cartId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get cart by user ID", description = "Retrieves the cart for a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    public ResponseEntity<CartDTO> getCartByUserId(@Parameter(description = "User ID") @PathVariable UUID userId) {
        return ResponseEntity.ok(cartService.findByUserId(userId));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{cartId}/items")
    @Operation(summary = "Add item to cart", description = "Adds a new item to the user's cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<CartItemDTO> addItemToCart(
            @Parameter(description = "Cart ID") @PathVariable UUID cartId,
            @RequestBody CartItemDTO cartItemDTO) {

        return new ResponseEntity<>(cartService.addItemToCart(cartId, cartItemDTO), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item", description = "Updates the quantity of an item in the cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item updated successfully"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<CartItemDTO> updateCartItem(
            @Parameter(description = "Cart item ID") @PathVariable UUID itemId,
            @RequestBody CartItemDTO cartItemDTO) {
        return ResponseEntity.ok(cartService.updateCartItem(itemId, cartItemDTO));
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Removes an item from the user's cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item removed successfully"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<Void> removeItemFromCart(@Parameter(description = "Cart item ID") @PathVariable UUID itemId) {
        cartService.removeItemFromCart(itemId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{cartId}/checkout")
    @Operation(summary = "Checkout cart", description = "Processes the checkout for a user's cart and creates an order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid checkout details")
    })
    public ResponseEntity<OrderDTO> checkout(
            @Parameter(description = "Cart ID") @PathVariable UUID cartId,
            @RequestParam String address) {
        return new ResponseEntity<>(cartService.checkout(cartId, address), HttpStatus.CREATED);
    }
}
