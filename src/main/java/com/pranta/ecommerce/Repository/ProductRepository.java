package com.pranta.ecommerce.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranta.ecommerce.Entity.Product;

public interface ProductRepository extends JpaRepository<Product,Long>{

    Optional<Product> findByName(String name);
}
