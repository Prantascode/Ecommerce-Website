package com.pranta.ecommerce.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranta.ecommerce.Entity.Brand;
import com.pranta.ecommerce.Entity.Category;
import com.pranta.ecommerce.Entity.Product;


public interface ProductRepository extends JpaRepository<Product,Long>{

    Optional<Product> findByName(String name);

    List<Product> findByCategory(Category category);

    List<Product> findByStock(int stock);

    List<Product> findByBrand(Brand brand);

    List<Product> findByColor(String color);
}
