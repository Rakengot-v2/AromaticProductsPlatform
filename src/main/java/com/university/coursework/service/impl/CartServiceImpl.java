package com.university.coursework.service.impl;

import com.university.coursework.domain.CartDTO;
import com.university.coursework.domain.CartItemDTO;
import com.university.coursework.domain.OrderDTO;
import com.university.coursework.entity.CartEntity;
import com.university.coursework.entity.CartItemEntity;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.exception.CartNotFoundException;
import com.university.coursework.exception.EmptyCartException;
import com.university.coursework.exception.ProductNotFoundException;
import com.university.coursework.repository.CartItemRepository;
import com.university.coursework.repository.CartRepository;
import com.university.coursework.repository.ProductRepository;
import com.university.coursework.service.CartService;
import com.university.coursework.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;

    @Override
    public CartDTO findByUserId(UUID userId) {
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user with id: " + userId));

        List<CartItemEntity> items = cartItemRepository.findByCartId(cart.getId());
        return mapCartToDto(cart, items);
    }

    @Override
    public CartDTO findById(UUID cartId) {
        CartEntity cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found with id: " + cartId));

        List<CartItemEntity> items = cartItemRepository.findByCartId(cart.getId());
        return mapCartToDto(cart, items);
    }

    @Override
    public CartItemDTO addItemToCart(UUID cartId, CartItemDTO cartItemDTO) {
        CartEntity cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found with id: " + cartId));

        ProductEntity product = productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + cartItemDTO.getProductId()));

        Optional<CartItemEntity> existingItem = cartItemRepository.findByCartIdAndProductId(cartId, cartItemDTO.getProductId());

        CartItemEntity cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + cartItemDTO.getQuantity());
        } else {
            cartItem = new CartItemEntity();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(cartItemDTO.getQuantity());
            cartItem.setPrice(product.getPrice());
        }

        CartItemEntity savedItem = cartItemRepository.save(cartItem);
        return mapCartItemToDto(savedItem);
    }

    @Override
    public CartItemDTO updateCartItem(UUID itemId, CartItemDTO cartItemDTO) {
        CartItemEntity cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ProductNotFoundException("Cart item not found with id: " + itemId));

        cartItem.setQuantity(cartItemDTO.getQuantity());
        CartItemEntity updatedItem = cartItemRepository.save(cartItem);
        return mapCartItemToDto(updatedItem);
    }

    @Override
    public void removeItemFromCart(UUID itemId) {
        if (!cartItemRepository.existsById(itemId)) {
            throw new ProductNotFoundException("Cart item not found with id: " + itemId);
        }
        cartItemRepository.deleteById(itemId);
    }

    @SneakyThrows
    @Override
    public OrderDTO checkout(UUID cartId, String address) {
        CartEntity cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found with id: " + cartId));


        List<CartItemEntity> cartItems = cartItemRepository.findByCartId(cartId);
        if (cartItems.isEmpty()) {
            throw new EmptyCartException("Cart is empty, cannot proceed to checkout");
        }

        if (cart.getItems() != null) {
            cart.getItems().clear();
        }

        cartRepository.save(cart);

        return orderService.createOrderFromCart(cart, cartItems, address);
    }

    private CartDTO mapCartToDto(CartEntity cart, List<CartItemEntity> items) {
        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(items.stream()
                        .map(this::mapCartItemToDto)
                        .collect(Collectors.toList()))
                .createdAt(cart.getCreatedAt())
                .build();
    }

    private CartItemDTO mapCartItemToDto(CartItemEntity entity) {
        return CartItemDTO.builder()
                .cartId(entity.getCart().getId())
                .productId(entity.getProduct().getId())
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .build();
    }
}