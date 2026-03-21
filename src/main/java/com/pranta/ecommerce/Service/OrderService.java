package com.pranta.ecommerce.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.OrderRequestDto;
import com.pranta.ecommerce.Dto.OrderResponseDto;
import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Repository.OderItemRepository;
import com.pranta.ecommerce.Repository.OrderRepository;
import com.pranta.ecommerce.Repository.UserRepository;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OderItemRepository oderItemRepository;

    public OrderResponseDto createOrder(OrderRequestDto dto ){

        User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(()-> new RuntimeException("User not Found"));
        
        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(dto.getTotalAmount());
        order.setStatus(dto.getStatus());
        order.setCreatedAt(dto.getCreatedAt());
        Order savedOrder = orderRepository.save(order);

        return mapToResponse(savedOrder);
    }

    private OrderResponseDto mapToResponse(Order order){
        return new OrderResponseDto(
            order.getId(),
            order.getUser().getId(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getCreatedAt()
        );
    }
}
