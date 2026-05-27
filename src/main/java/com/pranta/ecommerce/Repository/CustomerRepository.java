package com.pranta.ecommerce.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.pranta.ecommerce.Entity.Customer;
import com.pranta.ecommerce.Entity.User;

@EnableJpaRepositories
public interface CustomerRepository extends JpaRepository<Customer, Long>{
    
    Optional<Customer> findByUser(User user);

    boolean existsByPhoneAndIdNot(String phone, Long id);
}
