package com.pranta.ecommerce.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranta.ecommerce.Entity.Product;

public interface ProductRepository extends JpaRepository<Product,Long>{
    
}
