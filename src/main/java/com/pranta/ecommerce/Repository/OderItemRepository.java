package com.pranta.ecommerce.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranta.ecommerce.Entity.OrderItem;

public interface OderItemRepository extends JpaRepository<OrderItem,Long> {
    
}
