package com.hcl.inventory.controller;

import com.hcl.inventory.entity.Product;
import com.hcl.inventory.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {

        return ResponseEntity.ok(
                productService.getAllProducts()
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(
            @PathVariable Integer productId
    ) {

        return ResponseEntity.ok(
                productService.getProductById(productId)
        );
    }

    @PostMapping
    public ResponseEntity<Product> addProduct(
            @Valid @RequestBody Product product
    ) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.addProduct(product));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Integer productId,
            @Valid @RequestBody Product product
    ) {

        return ResponseEntity.ok(
                productService.updateProduct(productId, product)
        );
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Integer productId
    ) {

        productService.deleteProduct(productId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<Product>> searchByName(
            @RequestParam String name
    ) {

        return ResponseEntity.ok(
                productService.searchByName(name)
        );
    }

    @GetMapping("/search/category")
    public ResponseEntity<List<Product>> searchByCategory(
            @RequestParam String category
    ) {

        return ResponseEntity.ok(
                productService.searchByCategory(category)
        );
    }

    @GetMapping("/search/brand")
    public ResponseEntity<List<Product>> searchByBrand(
            @RequestParam String brand
    ) {

        return ResponseEntity.ok(
                productService.searchByBrand(brand)
        );
    }
}