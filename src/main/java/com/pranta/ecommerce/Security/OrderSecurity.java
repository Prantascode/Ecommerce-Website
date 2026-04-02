package com.pranta.ecommerce.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pranta.ecommerce.Repository.OrderRepository;

@Component("orderSecurity")
public class OrderSecurity {
    
    @Autowired
    private OrderRepository orderRepository;

    public boolean isOwner(Long orderId, String email){
        return orderRepository.findById(orderId)
            .map(order -> order.getUser().getEmail().equals(email))
            .orElse(false);
    }
}
