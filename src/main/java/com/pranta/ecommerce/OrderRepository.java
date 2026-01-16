package com.pranta.ecommerce;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Entity.User;

import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long>{
    
    List<Order> findByUser(User user);
}
