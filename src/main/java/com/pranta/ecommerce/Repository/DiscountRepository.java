package com.pranta.ecommerce.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pranta.ecommerce.Entity.Discount;

public interface DiscountRepository extends JpaRepository<Discount, Long> {

    List<Discount> findByProductId(Long productId);

    Optional<Discount> findByProductIdAndActiveTrue(Long productId);

    List<Discount> findByActiveTrue();

    @Query("SELECT d FROM Discount d WHERE d.active = true AND d.discountEndDate IS NOT NULL AND d.discountEndDate < CURRENT_TIMESTAMP")
    List<Discount> findExpiredActiveDiscounts();

    @Query("SELECT d FROM Discount d WHERE d.active = false AND d.discountStartDate IS NOT NULL AND d.discountStartDate <= CURRENT_TIMESTAMP AND (d.discountEndDate IS NULL OR d.discountEndDate > CURRENT_TIMESTAMP)")
    List<Discount> findPendingDiscountsToActivate();
}