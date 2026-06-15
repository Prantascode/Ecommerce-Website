package com.pranta.ecommerce.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranta.ecommerce.Entity.Wishlist;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    
    List<Wishlist> findAllWishlistsByUserId(Long userId);

    Optional<Wishlist> findWishlistsByUserId(Long userId);
    
}
