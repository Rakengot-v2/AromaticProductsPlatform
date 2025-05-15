package com.university.coursework.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.university.coursework.domain.CartDTO;
import com.university.coursework.domain.CartItemDTO;
import com.university.coursework.domain.OrderDTO;
import com.university.coursework.entity.CartEntity;
import com.university.coursework.entity.CartItemEntity;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.entity.UserEntity;
import com.university.coursework.repository.CartItemRepository;
import com.university.coursework.repository.CartRepository;
import com.university.coursework.repository.ProductRepository;
import com.university.coursework.service.impl.CartServiceImpl;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private OrderService orderService;

    private CartServiceImpl cartService;

    private UUID cartId;
    private UUID productId;
    private UUID itemId;
    private CartEntity cartEntity;
    private CartItemEntity cartItemEntity;
    private CartItemDTO cartItemDTO;


    @BeforeEach
    void setUp() {
        cartService = new CartServiceImpl(cartRepository, productRepository, cartItemRepository, orderService);
        cartId = UUID.randomUUID();
        productId = UUID.randomUUID();
        itemId = UUID.randomUUID();


        cartEntity = new CartEntity();
        cartEntity.setId(cartId);
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId(productId);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID());

        cartItemEntity = new CartItemEntity();
        cartItemEntity.setId(itemId);
        cartItemEntity.setProduct(productEntity);
        cartEntity.setUser(userEntity);
        cartItemEntity.setCart(cartEntity);
        cartItemEntity.setQuantity(1);

        cartItemDTO = CartItemDTO.builder()
                .cartId(cartId)
                .productId(productId)
                .quantity(2)
                .build();
    }

    @Test
    void testFindByUserId() {
        when(cartRepository.findByUserId(any(UUID.class))).thenReturn(Optional.of(cartEntity));
        when(cartItemRepository.findByCartId(cartId)).thenReturn(List.of(cartItemEntity));

        CartDTO result = cartService.findByUserId(cartId);

        assertNotNull(result);
        assertEquals(cartId, result.getId());
        verify(cartRepository).findByUserId(cartId);
    }

    @Test
    void testAddItemToCart() {
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cartEntity));
        when(productRepository.findById(productId)).thenReturn(Optional.of(new ProductEntity()));
        when(cartItemRepository.save(any(CartItemEntity.class))).thenReturn(cartItemEntity);

        CartItemDTO result = cartService.addItemToCart(cartId, cartItemDTO);

        assertNotNull(result);
        //assertEquals(itemId, result.getId());
        verify(cartItemRepository).save(any(CartItemEntity.class));
    }

    @Test
    void testUpdateCartItem() {
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(cartItemEntity));
        when(cartItemRepository.save(any(CartItemEntity.class))).thenReturn(cartItemEntity);

        CartItemDTO result = cartService.updateCartItem(itemId, cartItemDTO);

        assertNotNull(result);
        assertEquals(2, result.getQuantity());
    }

    @Test
    void testRemoveItemFromCart() {
        when(cartItemRepository.existsById(itemId)).thenReturn(true);
        cartService.removeItemFromCart(itemId);

        verify(cartItemRepository).deleteById(itemId);
    }

    @Test
    void testCheckout() {
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cartEntity));
        when(cartItemRepository.findByCartId(cartId)).thenReturn(List.of(cartItemEntity));
        when(orderService.createOrderFromCart(any(), any(), any())).thenReturn(new OrderDTO());

        OrderDTO order = cartService.checkout(cartId, "Test Address");

        assertNotNull(order);
        verify(orderService).createOrderFromCart(any(), any(), eq("Test Address"));
    }
}
