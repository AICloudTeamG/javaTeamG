package com.example.javaTeamG.repository;

import com.example.javaTeamG.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; 

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Product findByJanCode(String janCode);
    Optional<Product> findByName(String name);
}