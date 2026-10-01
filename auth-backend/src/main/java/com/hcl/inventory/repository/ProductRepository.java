package com.hcl.inventory.repository;

import com.hcl.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndProductIdNot(String sku, Integer productId);

    List<Product> findByProductNameContainingIgnoreCase(String productName);

    List<Product> findByProductCategoryIgnoreCase(String productCategory);

    List<Product> findByBrandContainingIgnoreCase(String brand);
}