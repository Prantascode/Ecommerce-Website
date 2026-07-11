package com.pranta.ecommerce.Repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.pranta.ecommerce.Entity.Brand;
import com.pranta.ecommerce.Entity.Category;
import com.pranta.ecommerce.Entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByName(String name);

    Page<Product> findByCategory(Category category, Pageable pageable);

    Page<Product> findByStock(int stock, Pageable pageable);

    Page<Product> findByBrand(Brand brand, Pageable pageable);

    Page<Product> findByColor(String color, Pageable pageable);

    Page<Product> findByStockLessThanEqual(int threshold, Pageable pageable);

    Page<Product> findByPriceBetween(double minPrice, double maxPrice, Pageable pageable);
}