package com.pranta.ecommerce.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Entity.OrderItem;
import com.pranta.ecommerce.Entity.Product;



public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {

    Optional<OrderItem> findByOrder(Long orderId);

    List<OrderItem> findByOrder(Order order);
    
    Optional<Product> findByProduct(Product product);
}
