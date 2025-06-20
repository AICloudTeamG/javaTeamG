// src/main/java/com/example/javaTeamG/service/ProductService.java
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
            throw new IllegalArgumentException("JANコード '" + product.getJanCode() + "' は既に存在します。");
        }
        // 商品名の重複チェック (Optional<Product> を使用)
        if (productRepository.findByName(product.getName()).isPresent()) {
            throw new IllegalArgumentException("商品名 '" + product.getName() + "' は既に存在します。");
        }
        product.setDeleted(false);
        return productRepository.save(product);
    }

    public Optional<Product> findProductById(Integer id) {
        return productRepository.findById(id);
    }

    public List<Product> findAllProducts() {
        // 論理削除された商品を除く場合は、リポジトリに findByIsDeletedFalse() を追加し、
        return productRepository.findByIsDeletedFalse(); // この行を優先
        // return productRepository.findAll(); // この行はコメントアウトまたは削除
    }

    // Optional<Product> を返すように変更
    public Optional<Product> findProductByName(String productName) {
        return productRepository.findByName(productName);
    }

    @Transactional
    public Product updateProduct(Integer id, Product updatedProduct) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    // 更新対象以外のJANコードの重複チェック
                    productRepository.findByIdIsNotAndJanCode(id, updatedProduct.getJanCode())
                            .ifPresent(p -> {
                                throw new IllegalArgumentException("JANコード '" + updatedProduct.getJanCode() + "' は他の商品で既に使用されています。");
                            });

                    // 更新対象以外の名前の重複チェック
                    productRepository.findByIdIsNotAndName(id, updatedProduct.getName())
                            .ifPresent(p -> {
                                throw new IllegalArgumentException("商品名 '" + updatedProduct.getName() + "' は他の商品で既に使用されています。");
                            });

                    existingProduct.setJanCode(updatedProduct.getJanCode());
                    existingProduct.setName(updatedProduct.getName());
                    existingProduct.setPrice(updatedProduct.getPrice());
                    // 削除フラグは更新時にも維持されるようにする（またはフォームから受け取る）
                    // product.setDeleted(updatedProduct.isDeleted()); // 必要に応じて
                    return productRepository.save(existingProduct);
                })
                .orElseThrow(() -> new IllegalArgumentException("ID " + id + " の商品が見つかりません。")); // RuntimeExceptionをIllegalArgumentExceptionに変更
    }

    @Transactional
    public void deleteProduct(Integer id) { // 戻り値は void のまま
        productRepository.findById(id)
                .ifPresent(product -> {
                    if (!product.isDeleted()) { // 既に削除済みでなければ論理削除
                        product.setDeleted(true);
                        productRepository.save(product);
                    }
                });
        // voidなので、削除の成否を呼び出し元に直接伝えることはできないが、
        // コントローラー側でOptionalが空の場合にエラーと判断することは可能
    }
}