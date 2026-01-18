package com.pranta.ecommerce.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranta.ecommerce.Entity.CartItem;
import com.pranta.ecommerce.Entity.User;

public interface CartRepository extends JpaRepository<CartItem,Long> {
    List<CartItem> findByUser(User user);

    void deleteByUser(User user);
}
