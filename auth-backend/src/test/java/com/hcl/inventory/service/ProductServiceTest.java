package com.hcl.inventory.service;

import com.hcl.inventory.entity.Product;
import com.hcl.inventory.enums.ProductStatus;
import com.hcl.inventory.exception.DuplicateSkuException;
import com.hcl.inventory.exception.ProductNotFoundException;
import com.hcl.inventory.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ProductService}.
 * The repository is mocked — no database required.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setProductId(1);
        sampleProduct.setProductName("Laptop Pro");
        sampleProduct.setSku("ELEC-LP-001");
        sampleProduct.setProductCategory("Electronics");
        sampleProduct.setProductPrice(new BigDecimal("79999.99"));
        sampleProduct.setUnit("pcs");
        sampleProduct.setStatus(ProductStatus.ACTIVE);
    }

    // ---------------------------------------------------------------
    // getAllProducts
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getAllProducts returns all products from repository")
    void getAllProducts_returnsAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct));

        List<Product> result = productService.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSku()).isEqualTo("ELEC-LP-001");
        verify(productRepository).findAll();
    }

    // ---------------------------------------------------------------
    // getProductById
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getProductById returns product when id exists")
    void getProductById_found() {
        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));

        Product result = productService.getProductById(1);

        assertThat(result.getProductName()).isEqualTo("Laptop Pro");
    }

    @Test
    @DisplayName("getProductById throws ProductNotFoundException when id missing")
    void getProductById_notFound_throwsException() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---------------------------------------------------------------
    // addProduct
    // ---------------------------------------------------------------

    @Test
    @DisplayName("addProduct saves and returns the new product")
    void addProduct_success() {
        when(productRepository.existsBySku("ELEC-LP-001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        Product result = productService.addProduct(sampleProduct);

        assertThat(result.getSku()).isEqualTo("ELEC-LP-001");
        verify(productRepository).save(sampleProduct);
    }

    @Test
    @DisplayName("addProduct throws DuplicateSkuException when SKU already exists")
    void addProduct_duplicateSku_throwsException() {
        when(productRepository.existsBySku("ELEC-LP-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.addProduct(sampleProduct))
                .isInstanceOf(DuplicateSkuException.class)
                .hasMessageContaining("ELEC-LP-001");

        verify(productRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // updateProduct
    // ---------------------------------------------------------------

    @Test
    @DisplayName("updateProduct persists changes and returns updated entity")
    void updateProduct_success() {
        Product updated = new Product();
        updated.setProductName("Laptop Pro 2");
        updated.setSku("ELEC-LP-001");
        updated.setProductCategory("Electronics");
        updated.setProductPrice(new BigDecimal("89999.99"));
        updated.setUnit("pcs");

        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.existsBySkuAndProductIdNot("ELEC-LP-001", 1)).thenReturn(false);
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.updateProduct(1, updated);

        assertThat(result.getProductName()).isEqualTo("Laptop Pro 2");
    }

    @Test
    @DisplayName("updateProduct throws ProductNotFoundException when id not found")
    void updateProduct_notFound_throwsException() {
        when(productRepository.findById(42)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(42, sampleProduct))
                .isInstanceOf(ProductNotFoundException.class);
    }

    // ---------------------------------------------------------------
    // deleteProduct
    // ---------------------------------------------------------------

    @Test
    @DisplayName("deleteProduct removes the entity from the repository")
    void deleteProduct_success() {
        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));
        doNothing().when(productRepository).delete(sampleProduct);

        productService.deleteProduct(1);

        verify(productRepository).delete(sampleProduct);
    }

    @Test
    @DisplayName("deleteProduct throws ProductNotFoundException when id missing")
    void deleteProduct_notFound_throwsException() {
        when(productRepository.findById(5)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(5))
                .isInstanceOf(ProductNotFoundException.class);
    }

    // ---------------------------------------------------------------
    // searchByName / searchByCategory / searchByBrand
    // ---------------------------------------------------------------

    @Test
    @DisplayName("searchByName delegates to repository and returns results")
    void searchByName_returnsMatchingProducts() {
        when(productRepository.findByProductNameContainingIgnoreCase("laptop"))
                .thenReturn(List.of(sampleProduct));

        List<Product> result = productService.searchByName("laptop");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("searchByCategory returns products in the given category")
    void searchByCategory_returnsMatchingProducts() {
        when(productRepository.findByProductCategoryIgnoreCase("Electronics"))
                .thenReturn(List.of(sampleProduct));

        List<Product> result = productService.searchByCategory("Electronics");

        assertThat(result).hasSize(1);
    }
}
