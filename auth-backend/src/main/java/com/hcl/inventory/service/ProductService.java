package com.hcl.inventory.service;

import com.hcl.inventory.entity.Product;
import com.hcl.inventory.enums.ProductStatus;
import com.hcl.inventory.exception.DuplicateSkuException;
import com.hcl.inventory.exception.ProductNotFoundException;
import com.hcl.inventory.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Integer productId) {

        return productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product with id " + productId + " not found"
                        )
                );
    }

    public Product addProduct(Product product) {

        if (productRepository.existsBySku(product.getSku())) {
            throw new DuplicateSkuException(
                    "Product with SKU " + product.getSku() + " already exists"
            );
        }

        if (product.getStatus() == null) {
            product.setStatus(ProductStatus.ACTIVE);
        }

        return productRepository.save(product);
    }

    public Product updateProduct(Integer productId, Product updatedProduct) {

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product with id " + productId + " not found"
                        )
                );

        if (productRepository.existsBySkuAndProductIdNot(
                updatedProduct.getSku(),
                productId
        )) {
            throw new DuplicateSkuException(
                    "Product with SKU " + updatedProduct.getSku() + " already exists"
            );
        }

        existingProduct.setProductName(updatedProduct.getProductName());
        existingProduct.setProductPrice(updatedProduct.getProductPrice());
        existingProduct.setProductCategory(updatedProduct.getProductCategory());
        existingProduct.setSku(updatedProduct.getSku());
        existingProduct.setBrand(updatedProduct.getBrand());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setUnit(updatedProduct.getUnit());

        if (updatedProduct.getStatus() != null) {
            existingProduct.setStatus(updatedProduct.getStatus());
        }

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(Integer productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product with id " + productId + " not found"
                        )
                );

        productRepository.delete(product);
    }

    public List<Product> searchByName(String productName) {
        return productRepository.findByProductNameContainingIgnoreCase(productName);
    }

    public List<Product> searchByCategory(String productCategory) {
        return productRepository.findByProductCategoryIgnoreCase(productCategory);
    }

    public List<Product> searchByBrand(String brand) {
        return productRepository.findByBrandContainingIgnoreCase(brand);
    }
}