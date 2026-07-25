package com.pranta.ecommerce.Repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Entity.User.Role;



public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Page<User> findAll(Pageable pageable);

    Page<User> findByActive(boolean active,Pageable pageable);

    Page<User> findByRole(Role role,Pageable pageable);

    boolean existsByEmail(String newEmail);
}
