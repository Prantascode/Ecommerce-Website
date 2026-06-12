package com.pranta.ecommerce.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranta.ecommerce.Entity.Category;

public interface CategoryRepository extends JpaRepository<Category,Long>{
    
}
