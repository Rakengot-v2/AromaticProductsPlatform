package com.university.coursework.service.impl;

import com.university.coursework.domain.OrderDTO;
import com.university.coursework.domain.OrderItemDTO;
import com.university.coursework.entity.CartEntity;
import com.university.coursework.entity.CartItemEntity;
import com.university.coursework.entity.OrderEntity;
import com.university.coursework.entity.OrderItemEntity;
import com.university.coursework.exception.OrderNotFoundException;
import com.university.coursework.repository.CartItemRepository;
import com.university.coursework.repository.OrderItemRepository;
import com.university.coursework.repository.OrderRepository;
import com.university.coursework.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public OrderDTO createOrderFromCart(CartEntity cart, List<CartItemEntity> cartItems, String address) {
        OrderEntity order = OrderEntity.builder()
                .user(cart.getUser())
                .address(address)
                .status("CREATED")
                .createdAt(cart.getCreatedAt())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        List<OrderItemEntity> orderItems = new ArrayList<>();
        for (CartItemEntity cartItem : cartItems) {
            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice())
                    .build();

            orderItems.add(orderItem);
            total = total.add(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotal(total);
        order.setItems(orderItems);
        OrderEntity savedOrder = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        cartItemRepository.deleteByCartId(cart.getId());

        return mapToDto(savedOrder);
    }

    @Override
    public List<OrderDTO> findAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> findOrdersByUserId(UUID userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO findOrderById(UUID id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        return mapToDto(order);
    }

    @Override
    public OrderDTO updateOrderStatus(UUID id, String status) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if (!isValidStatus(status)) {
            throw new RuntimeException("Invalid order status: " + status);
        }

        order.setStatus(status);
        OrderEntity updatedOrder = orderRepository.save(order);
        return mapToDto(updatedOrder);
    }

    private boolean isValidStatus(String status) {
        return List.of("CREATED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED").contains(status);
    }

    private OrderDTO mapToDto(OrderEntity entity) {
        return OrderDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .total(entity.getTotal())
                .status(entity.getStatus())
                .address(entity.getAddress())
                .createdAt(entity.getCreatedAt())
                .items(entity.getItems().stream()
                        .map(this::mapItemToDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private OrderItemDTO mapItemToDto(OrderItemEntity entity) {
        return OrderItemDTO.builder()
                .id(entity.getId())
                .orderId(entity.getOrder().getId())
                .productId(entity.getProduct().getId())
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .build();
    }
}