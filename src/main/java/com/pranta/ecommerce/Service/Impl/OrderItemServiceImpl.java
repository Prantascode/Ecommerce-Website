package com.pranta.ecommerce.Service.Impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.OrderItemResponseDto;
import com.pranta.ecommerce.Entity.CartItem;
import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Entity.OrderItem;
import com.pranta.ecommerce.Repository.OrderItemRepository;
import com.pranta.ecommerce.Service.OrderItemService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public List<OrderItemResponseDto> convertCartItemToOrderItem(List<CartItem> cartItems, Order order) {
        log.info("Converting {} cart items to order items for order ID: {}", cartItems.size(), order.getId());
        
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setOrder(order);
            orderItems.add(orderItem);
        }
        
        orderItemRepository.saveAll(orderItems);
        log.info("Successfully converted {} cart items to order items", orderItems.size());

        return orderItems.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private OrderItemResponseDto mapToResponse(OrderItem item) {
        return new OrderItemResponseDto(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPrice(),
                item.getOrder().getId()
        );
    }
}