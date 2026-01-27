package com.pranta.ecommerce.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Entity.OrderItem;

public interface OderItemRepository extends JpaRepository<OrderItem,Long> {

    Optional<OrderItem> findByOrder(Order order);
    
}
