package com.pranta.ecommerce.Repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranta.ecommerce.Entity.Brand;


public interface BrandRepository extends JpaRepository<Brand, Long> {

    Optional<Brand> findByName(String name);
}
