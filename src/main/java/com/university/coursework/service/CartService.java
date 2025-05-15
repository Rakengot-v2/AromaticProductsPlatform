package com.university.coursework.service;

import com.university.coursework.domain.CartDTO;
import com.university.coursework.domain.CartItemDTO;
import com.university.coursework.domain.OrderDTO;
import org.apache.coyote.BadRequestException;

import java.util.UUID;

public interface CartService {
    CartDTO findById(UUID cartId);
    CartDTO findByUserId(UUID userId);
    CartItemDTO addItemToCart(UUID cartId, CartItemDTO cartItemDTO);
    CartItemDTO updateCartItem(UUID itemId, CartItemDTO cartItemDTO);
    void removeItemFromCart(UUID itemId);
    OrderDTO checkout(UUID cartId, String address);
}