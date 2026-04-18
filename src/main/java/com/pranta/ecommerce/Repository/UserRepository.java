package com.pranta.ecommerce.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Entity.User.Role;



public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByActive(boolean active);

    List<User> findByRole(Role role);
}
