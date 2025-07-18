package com.example.javaTeamG.repository;

import com.example.javaTeamG.model.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; 
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Product findByJanCode(String janCode);
    Optional<Product> findByName(String name);
    Optional<Product> findByNameAndIsDeletedFalse(String name);
    List<Product> findByIsDeletedFalse();
        // IDとJANコードが一致しない他のProductで、JANコードが重複しているかチェック

    List<Product> findByIsDeletedFalseOrderByIdAsc();
    // 論理削除されていないスタッフをIDの昇順で取得するメソッド

    Optional<Product> findByIdIsNotAndJanCode(Integer id, String janCode);

    // IDと名前が一致しない他のProductで、名前が重複しているかチェック
    Optional<Product> findByIdIsNotAndName(Integer id, String name);

    // JANコードの重複チェックに使用するメソッド (論理削除されていないものに限る)
    Optional<Product> findByJanCodeAndIsDeletedFalse(String janCode);
}

