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
        // JANコードや商品名の重複チェックを追加することもできます
        if (productRepository.findByJanCode(product.getJanCode()) != null) {
            throw new IllegalArgumentException("Product with JAN code " + product.getJanCode() + " already exists.");
        }
        if (productRepository.findByName(product.getName()) != null) {
            throw new IllegalArgumentException("Product with name " + product.getName() + " already exists.");
        }
        product.setDeleted(false);
        return productRepository.save(product);
    }

    public Optional<Product> findProductById(Integer id) {
        return productRepository.findById(id);
    }

    public List<Product> findAllProducts() {
        return productRepository.findAll(); // 論理削除された商品を除く場合はカスタムクエリが必要
    }

    @Transactional
    public Product updateProduct(Integer id, Product updatedProduct) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setJanCode(updatedProduct.getJanCode());
                    product.setName(updatedProduct.getName());
                    product.setPrice(updatedProduct.getPrice());
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