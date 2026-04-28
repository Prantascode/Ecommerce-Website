package com.pranta.ecommerce.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pranta.ecommerce.Entity.Brand;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

}
