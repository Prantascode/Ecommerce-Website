package com.pranta.ecommerce.Repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.pranta.ecommerce.Entity.CartItem;

import jakarta.transaction.Transactional;


public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    @Modifying
    @Transactional
    void deleteAllByCartId(Long cartId);
    void deleteByCartId(Long id);

    List<CartItem> findByCartId(Long cartId);
}
