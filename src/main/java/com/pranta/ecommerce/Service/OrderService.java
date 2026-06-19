package com.pranta.ecommerce.Service;

import java.util.List;

import com.pranta.ecommerce.Dto.OrderResponseDto;
import com.pranta.ecommerce.Entity.Order.OrderStatus;

public interface OrderService {
    
    OrderResponseDto createOrder(String email);
    
    List<OrderResponseDto> getMyOrder(String email);
    
    List<OrderResponseDto> getAllOrder();
    
    OrderResponseDto getMyOrderById(Long orderId, String email);
    
    OrderResponseDto updateOrderStatus(Long orderId, OrderStatus newStatus);
    
    OrderResponseDto cancelOrder(Long orderId, String email);
    
    List<OrderResponseDto> filterByStatus(OrderStatus status);
}