package com.pranta.ecommerce.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranta.ecommerce.Entity.Customer;
import com.pranta.ecommerce.Entity.Product;
import com.pranta.ecommerce.Entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	boolean existsByCustomerAndProduct(Customer customer, Product product);

    List<Review> findByProduct(Product product);

    Optional<Review> findByProductId(Long productId);

    List<Review> findAllReviewsbyProductId(Long productId);
    
}
