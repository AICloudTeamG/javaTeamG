package com.example.javaTeamG.repository;

import com.example.javaTeamG.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Product findByJanCode(String janCode);
    Product findByName(String name);
}
