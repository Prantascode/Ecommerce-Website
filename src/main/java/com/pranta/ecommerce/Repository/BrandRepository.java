package com.pranta.ecommerce.Repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.pranta.ecommerce.Entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {

}
