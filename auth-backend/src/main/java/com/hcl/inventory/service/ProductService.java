package com.hcl.inventory.service;

import com.hcl.inventory.entity.Product;
import com.hcl.inventory.enums.ProductStatus;
import com.hcl.inventory.exception.DuplicateSkuException;
import com.hcl.inventory.exception.ProductNotFoundException;
import com.hcl.inventory.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        existingProduct.setImageUrl(updatedProduct.getImageUrl());
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

    @Transactional
    public Map<String, Object> bulkImportFromCsv(MultipartFile file) {
        int importedCount = 0;
        int skippedCount = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (isHeader) {
                    isHeader = false;
                    // Check if it's indeed header
                    if (line.toLowerCase().contains("sku") || line.toLowerCase().contains("productname")) {
                        continue;
                    }
                }

                String[] tokens = parseCsvLine(line);
                if (tokens.length < 5) {
                    errors.add("Line " + lineNumber + ": Insufficient columns");
                    skippedCount++;
                    continue;
                }

                try {
                    // Expect format: productName, productPrice, productCategory, sku, brand, description, unit, status, imageUrl
                    String name = tokens[0].trim();
                    BigDecimal price = new BigDecimal(tokens[1].trim());
                    String category = tokens[2].trim();
                    String sku = tokens[3].trim().toUpperCase();
                    String brand = tokens.length > 4 ? tokens[4].trim() : "";
                    String description = tokens.length > 5 ? tokens[5].trim() : "";
                    String unit = tokens.length > 6 ? tokens[6].trim() : "pcs";
                    String statusStr = tokens.length > 7 ? tokens[7].trim() : "ACTIVE";
                    String imageUrl = tokens.length > 8 ? tokens[8].trim() : "";

                    ProductStatus status = ProductStatus.ACTIVE;
                    try {
                        status = ProductStatus.valueOf(statusStr.toUpperCase());
                    } catch (IllegalArgumentException ignored) {
                    }

                    if (productRepository.existsBySku(sku)) {
                        skippedCount++;
                        continue;
                    }

                    Product product = new Product();
                    product.setProductName(name);
                    product.setProductPrice(price);
                    product.setProductCategory(category);
                    product.setSku(sku);
                    product.setBrand(brand);
                    product.setDescription(description);
                    product.setUnit(unit.isEmpty() ? "pcs" : unit);
                    product.setStatus(status);
                    product.setImageUrl(imageUrl);

                    productRepository.save(product);
                    importedCount++;
                } catch (Exception ex) {
                    errors.add("Line " + lineNumber + ": " + ex.getMessage());
                    skippedCount++;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage(), e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("importedCount", importedCount);
        result.put("skippedCount", skippedCount);
        result.put("errors", errors);
        result.put("status", "SUCCESS");
        return result;
    }

    public byte[] exportToCsv() {
        List<Product> products = productRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("productId,productName,productPrice,productCategory,sku,brand,description,unit,status,imageUrl\n");

        for (Product p : products) {
            sb.append(escapeCsv(String.valueOf(p.getProductId()))).append(",");
            sb.append(escapeCsv(p.getProductName())).append(",");
            sb.append(p.getProductPrice() != null ? p.getProductPrice().toString() : "0.00").append(",");
            sb.append(escapeCsv(p.getProductCategory())).append(",");
            sb.append(escapeCsv(p.getSku())).append(",");
            sb.append(escapeCsv(p.getBrand())).append(",");
            sb.append(escapeCsv(p.getDescription())).append(",");
            sb.append(escapeCsv(p.getUnit())).append(",");
            sb.append(escapeCsv(p.getStatus() != null ? p.getStatus().name() : "ACTIVE")).append(",");
            sb.append(escapeCsv(p.getImageUrl())).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens.toArray(new String[0]);
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
