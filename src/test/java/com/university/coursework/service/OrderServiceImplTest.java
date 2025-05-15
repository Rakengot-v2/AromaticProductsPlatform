package com.university.coursework.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.university.coursework.domain.OrderDTO;
import com.university.coursework.entity.*;
import com.university.coursework.exception.OrderNotFoundException;
import com.university.coursework.repository.CartItemRepository;
import com.university.coursework.repository.OrderItemRepository;
import com.university.coursework.repository.OrderRepository;
import com.university.coursework.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private CartItemRepository cartItemRepository;

    private OrderServiceImpl orderService;

    private UUID orderId;
    private UUID cartId;
    private UUID userId;
    private CartEntity cartEntity;
    private OrderEntity orderEntity;
    private OrderDTO orderDTO;
    private CartItemEntity cartItemEntity;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, orderItemRepository, cartItemRepository);
        orderId = UUID.randomUUID();
        cartId = UUID.randomUUID();
        userId = UUID.randomUUID();

        cartEntity = new CartEntity();
        cartEntity.setId(cartId);
        cartEntity.setUser(new UserEntity());

        orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        orderEntity.setUser(cartEntity.getUser());
        orderEntity.setTotal(BigDecimal.valueOf(100));
        orderEntity.setStatus("CREATED");
        orderEntity.setAddress("Test Address");

        cartItemEntity = new CartItemEntity();
        cartItemEntity.setCart(cartEntity);
        cartItemEntity.setQuantity(2);
        cartItemEntity.setPrice(BigDecimal.valueOf(50));

        orderDTO = OrderDTO.builder()
                .id(orderId)
                .userId(userId)
                .total(BigDecimal.valueOf(100))
                .status("CREATED")
                .address("Test Address")
                .build();
    }

    @Test
    void testCreateOrderFromCart() {
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);
        when(orderItemRepository.saveAll(any())).thenReturn(List.of(new OrderItemEntity()));

        OrderDTO result = orderService.createOrderFromCart(cartEntity, List.of(cartItemEntity), "Test Address");

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100), result.getTotal());
        assertEquals("CREATED", result.getStatus());
        verify(orderRepository).save(any(OrderEntity.class));
        verify(orderItemRepository).saveAll(any());
        verify(cartItemRepository).deleteByCartId(cartId);
    }

    @Test
    void testFindAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(orderEntity));

        List<OrderDTO> result = orderService.findAllOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void testFindOrdersByUserId() {
        when(orderRepository.findByUserId(userId)).thenReturn(List.of(orderEntity));

        List<OrderDTO> result = orderService.findOrdersByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findByUserId(userId);
    }

    @Test
    void testFindOrderById() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));

        OrderDTO result = orderService.findOrderById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void testFindOrderByIdNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.findOrderById(orderId));
    }

    @Test
    void testUpdateOrderStatus() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);

        OrderDTO result = orderService.updateOrderStatus(orderId, "SHIPPED");

        assertNotNull(result);
        assertEquals("SHIPPED", result.getStatus());
        verify(orderRepository).save(any(OrderEntity.class));
    }
}
