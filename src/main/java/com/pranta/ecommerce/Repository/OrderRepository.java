package com.pranta.ecommerce.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranta.ecommerce.Entity.Customer;
import com.pranta.ecommerce.Entity.Order;
import com.pranta.ecommerce.Entity.Order.OrderStatus;

import java.util.List;


public interface OrderRepository extends JpaRepository<Order,Long>{
    
    List<Order> findByCustomer(Customer customer);
    List<Order> findAllByCustomerIdOrderByOrderDateDesc(Long customerId);
    List<Order> findAll();
    List<Order> findByStatus(OrderStatus status);
}
