package com.university.coursework.service;

import com.university.coursework.domain.OrderDTO;
import com.university.coursework.entity.CartEntity;
import com.university.coursework.entity.CartItemEntity;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderDTO createOrderFromCart(CartEntity cart, List<CartItemEntity> cartItems, String address);
    List<OrderDTO> findAllOrders();
    List<OrderDTO> findOrdersByUserId(UUID userId);
    OrderDTO findOrderById(UUID id);
    OrderDTO updateOrderStatus(UUID id, String status);
}