// src/main/java/com/example/javaTeamG/model/ProductSalesEntry.java
package com.example.javaTeamG.model;

public class ProductSalesEntry {
    private String productName; // HTMLのhidden input name="performances[i].productName"
    private Integer quantity;    // HTMLのinput type="number" name="performances[i].quantity"

    // GetterとSetter

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "ProductSalesEntry{" +
               "productName='" + productName + '\'' +
               ", quantity=" + quantity +
               '}';
    }
}