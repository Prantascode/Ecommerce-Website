package com.pranta.ecommerce.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pranta.ecommerce.Entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long>{
    
}
