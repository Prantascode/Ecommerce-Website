package com.pranta.ecommerce.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pranta.ecommerce.Entity.Customer;
import com.pranta.ecommerce.Entity.User;


@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>{
    
    Optional<Customer> findByUser(User user);
}
