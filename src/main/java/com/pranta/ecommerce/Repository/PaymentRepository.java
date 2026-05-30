package com.pranta.ecommerce.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranta.ecommerce.Entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long>{
    
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByTransactionId(String transactionId);
}
