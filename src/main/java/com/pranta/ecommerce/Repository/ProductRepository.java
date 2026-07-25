package com.pranta.ecommerce.Repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pranta.ecommerce.Entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByName(String name);

    @Override
    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByStock(int stock, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByBrandId(Long brandId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByColor(String color, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByStockLessThanEqual(int threshold, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    @Query("""
            SELECT p
            FROM Product p
            WHERE p.stock > 0
            """)
    Page<Product> findByAvailableProduct(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    @Query("""
        SELECT p
        FROM Product p
        WHERE p.stock > 0
        AND EXISTS (SELECT 1 FROM Discount d WHERE d.product = p AND d.active = true)
    """)
    Page<Product> findAvailableProductsWithActiveDiscount(Pageable pageable);
}