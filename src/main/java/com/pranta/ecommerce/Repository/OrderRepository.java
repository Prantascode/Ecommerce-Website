package com.pranta.ecommerce.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Entity.Order.OrderStatus;
import com.pranta.ecommerce.Entity.User;

import java.util.List;
import java.util.Optional;


public interface OrderRepository extends JpaRepository<Order,Long>{
    
    List<Order> findByUser(User user);
    List<Order> findAllByUserIdOrderByOrderDateDesc(Long userId);
    List<Order> findAll();
    List<Order> findByStatus(OrderStatus status);
}
