package com.example.javaTeamG.service;

import com.example.javaTeamG.model.Product;
import com.example.javaTeamG.repository.ProductRepository;
import org.springframework.dao.DataIntegrityViolationException; // ★追加★
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAllProducts() {
        return productRepository.findByIsDeletedFalse();
    }

    public Optional<Product> findProductById(Integer id) {
        return productRepository.findById(id);
    }

    public Optional<Product> findProductByName(String name) {
        return productRepository.findByNameAndIsDeletedFalse(name);
    }

    @Transactional
    public Product createProduct(Product product) {
        // JANコードの重複チェック（Service層でメッセージをカスタマイズ）
        if (productRepository.findByJanCodeAndIsDeletedFalse(product.getJanCode()).isPresent()) {
            throw new IllegalArgumentException("JANコード「" + product.getJanCode() + "」は既に登録されています。");
        }
        // 商品名の重複チェック（Service層でメッセージをカスタマイズ）
        if (productRepository.findByNameAndIsDeletedFalse(product.getName()).isPresent()) {
            throw new IllegalArgumentException("商品名「" + product.getName() + "」は既に登録されています。");
        }
        product.setDeleted(false);
        try {
            product.setId(null);
            return productRepository.save(product);
        } catch (DataIntegrityViolationException e) {
            // DBレベルの制約違反 (例: unique=true, nullable=false, length=13) を捕捉
            // 既にService層で重複チェックしているため、JANコードの長さ違反などが主な原因となる
            if (product.getJanCode() != null && product.getJanCode().length() != 13) {
                throw new IllegalArgumentException("JANコードは13桁である必要があります。");
            }
            throw new IllegalArgumentException("データベース制約違反により商品登録に失敗しました。詳細: " + e.getMessage());
        }
    }

    @Transactional
    public Product updateProduct(Integer id, Product updatedProduct) {
        return productRepository.findById(id).map(existingProduct -> {
            // JANコードの重複チェック（ただし、自分自身は除く）
            Optional<Product> janCodeConflict = productRepository
                    .findByJanCodeAndIsDeletedFalse(updatedProduct.getJanCode());
            if (janCodeConflict.isPresent() && !janCodeConflict.get().getId().equals(existingProduct.getId())) {
                throw new IllegalArgumentException("JANコード「" + updatedProduct.getJanCode() + "」は他の商品で既に使用されています。");
            }
            // 商品名の重複チェック（ただし、自分自身は除く）
            Optional<Product> nameConflict = productRepository.findByNameAndIsDeletedFalse(updatedProduct.getName());
            if (nameConflict.isPresent() && !nameConflict.get().getId().equals(existingProduct.getId())) {
                throw new IllegalArgumentException("商品名「" + updatedProduct.getName() + "」は他の商品で既に使用されています。");
            }

            existingProduct.setJanCode(updatedProduct.getJanCode());
            existingProduct.setName(updatedProduct.getName());
            existingProduct.setPrice(updatedProduct.getPrice());
            try {
                return productRepository.save(existingProduct);
            } catch (DataIntegrityViolationException e) {
                // DBレベルの制約違反を捕捉
                if (updatedProduct.getJanCode() != null && updatedProduct.getJanCode().length() != 13) {
                    throw new IllegalArgumentException("JANコードは13桁である必要があります。");
                }
                throw new IllegalArgumentException("データベース制約違反により商品更新に失敗しました。詳細: " + e.getMessage());
            }
        }).orElseThrow(() -> new IllegalArgumentException("ID " + id + " の商品が見つかりません。"));
    }

    @Transactional
    public void deleteProduct(Integer id) {
        productRepository.findById(id).ifPresentOrElse(product -> {
            if (product.isDeleted()) {
                throw new IllegalArgumentException("商品ID " + id + " は既に削除済みです。");
            }
            product.setDeleted(true);
            productRepository.save(product);
        }, () -> {
            throw new IllegalArgumentException("商品ID " + id + " が見つかりません。");
        });
    }
}