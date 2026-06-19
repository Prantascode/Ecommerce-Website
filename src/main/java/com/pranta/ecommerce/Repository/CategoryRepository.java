package com.pranta.ecommerce.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranta.ecommerce.Entity.Category;


public interface CategoryRepository extends JpaRepository<Category,Long>{
    
    Optional<Category> findByName(String name);
}
