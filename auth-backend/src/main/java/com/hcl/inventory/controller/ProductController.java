package com.hcl.inventory.controller;

import com.hcl.inventory.entity.Product;
import com.hcl.inventory.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@Tag(name = "Product Catalog", description = "Product master catalog management, search, and bulk operations")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "List all products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(
                productService.getAllProducts()
        );
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<Product> getProductById(
            @PathVariable Integer productId
    ) {
        return ResponseEntity.ok(
                productService.getProductById(productId)
        );
    }

    @PostMapping
    @Operation(summary = "Create product")
    public ResponseEntity<Product> addProduct(
            @Valid @RequestBody Product product
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.addProduct(product));
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Update product")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Integer productId,
            @Valid @RequestBody Product product
    ) {
        return ResponseEntity.ok(
                productService.updateProduct(productId, product)
        );
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Delete product")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Integer productId
    ) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/name")
    @Operation(summary = "Search products by name")
    public ResponseEntity<List<Product>> searchByName(
            @RequestParam String name
    ) {
        return ResponseEntity.ok(
                productService.searchByName(name)
        );
    }

    @GetMapping("/search/category")
    @Operation(summary = "Filter products by category")
    public ResponseEntity<List<Product>> searchByCategory(
            @RequestParam String category
    ) {
        return ResponseEntity.ok(
                productService.searchByCategory(category)
        );
    }

    @GetMapping("/search/brand")
    @Operation(summary = "Filter products by brand")
    public ResponseEntity<List<Product>> searchByBrand(
            @RequestParam String brand
    ) {
        return ResponseEntity.ok(
                productService.searchByBrand(brand)
        );
    }

    @PostMapping(value = "/bulk-import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Bulk import products from CSV")
    public ResponseEntity<Map<String, Object>> bulkImport(
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(productService.bulkImportFromCsv(file));
    }

    @GetMapping(value = "/export/csv", produces = "text/csv")
    @Operation(summary = "Export product catalog to CSV")
    public ResponseEntity<byte[]> exportCsv() {
        byte[] csv = productService.exportToCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"products.csv\"")
                .body(csv);
    }
}
