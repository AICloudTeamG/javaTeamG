package com.example.javaTeamG.service;

import com.example.javaTeamG.model.Product;
import com.example.javaTeamG.repository.ProductRepository;
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

    @Transactional
    public Product createProduct(Product product) {
        // JANコードの重複チェック
        if (productRepository.findByJanCode(product.getJanCode()) != null) {
            throw new IllegalArgumentException("Product with JAN code " + product.getJanCode() + " already exists.");
        }
        // 商品名の重複チェック (Optional<Product> を使用)
        if (productRepository.findByName(product.getName()).isPresent()) {
            throw new IllegalArgumentException("Product with name " + product.getName() + " already exists.");
        }
        product.setDeleted(false);
        return productRepository.save(product);
    }

    public Optional<Product> findProductById(Integer id) {
        return productRepository.findById(id);
    }

    public List<Product> findAllProducts() {
        // 論理削除された商品を除く場合は、リポジトリに findByIsDeletedFalse() を追加し、
        // return productRepository.findByIsDeletedFalse(); を使用することを推奨します。
        return productRepository.findAll();
    }

    // Optional<Product> を返すように変更
    public Optional<Product> findProductByName(String productName) {
        return productRepository.findByName(productName);
    }

    @Transactional
    public Product updateProduct(Integer id, Product updatedProduct) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setJanCode(updatedProduct.getJanCode());
                    product.setName(updatedProduct.getName());
                    product.setPrice(updatedProduct.getPrice());
                    // 削除フラグは更新時にも維持されるようにする（またはフォームから受け取る）
                    // product.setDeleted(updatedProduct.isDeleted()); // 必要に応じて
                    return productRepository.save(product);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Transactional
    public void deleteProduct(Integer id) {
        productRepository.findById(id)
                .ifPresent(product -> {
                    product.setDeleted(true);
                    productRepository.save(product);
                });
    }
}