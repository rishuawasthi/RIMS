package com.hcl.inventory.service;

import com.hcl.inventory.entity.Inventory;
import com.hcl.inventory.entity.Product;
import com.hcl.inventory.entity.Warehouse;
import com.hcl.inventory.enums.InventoryStatus;
import com.hcl.inventory.exception.DuplicateInventoryException;
import com.hcl.inventory.exception.InventoryNotFoundException;
import com.hcl.inventory.repository.InventoryRepository;
import com.hcl.inventory.repository.ProductRepository;
import com.hcl.inventory.repository.WarehouseRepository;
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
 * Unit tests for {@link InventoryService}.
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product product;
    private Warehouse warehouse;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setProductId(10);
        product.setProductName("T-Shirt");
        product.setSku("APP-TS-010");
        product.setProductCategory("Apparel");
        product.setProductPrice(new BigDecimal("499.00"));
        product.setUnit("pcs");

        warehouse = new Warehouse();
        warehouse.setWarehouseId(5);
        warehouse.setWarehouseName("Mumbai Hub");

        inventory = new Inventory();
        inventory.setInventoryId(1);
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQuantity(100);
        inventory.setReorderLevel(20);
        inventory.setReservedQuantity(10);
        inventory.setStatus(InventoryStatus.ACTIVE);
    }

    // ---------------------------------------------------------------
    // getAllInventory
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getAllInventory returns all entries from repository")
    void getAllInventory_returnsAll() {
        when(inventoryRepository.findAll()).thenReturn(List.of(inventory));

        List<Inventory> result = inventoryService.getAllInventory();

        assertThat(result).hasSize(1);
        verify(inventoryRepository).findAll();
    }

    // ---------------------------------------------------------------
    // getInventoryById
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getInventoryById returns inventory when id exists")
    void getInventoryById_found() {
        when(inventoryRepository.findById(1)).thenReturn(Optional.of(inventory));

        Inventory result = inventoryService.getInventoryById(1);

        assertThat(result.getQuantity()).isEqualTo(100);
    }

    @Test
    @DisplayName("getInventoryById throws InventoryNotFoundException when missing")
    void getInventoryById_notFound_throwsException() {
        when(inventoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getInventoryById(99))
                .isInstanceOf(InventoryNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---------------------------------------------------------------
    // addInventory
    // ---------------------------------------------------------------

    @Test
    @DisplayName("addInventory saves record when product + warehouse exist and combo is unique")
    void addInventory_success() {
        when(productRepository.findById(10)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(5)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.existsByProductProductIdAndWarehouseWarehouseId(10, 5))
                .thenReturn(false);
        when(inventoryRepository.save(any())).thenReturn(inventory);

        Inventory result = inventoryService.addInventory(inventory);

        assertThat(result.getInventoryId()).isEqualTo(1);
        verify(inventoryRepository).save(any());
    }

    @Test
    @DisplayName("addInventory throws DuplicateInventoryException when combo already exists")
    void addInventory_duplicate_throwsException() {
        when(productRepository.findById(10)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(5)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.existsByProductProductIdAndWarehouseWarehouseId(10, 5))
                .thenReturn(true);

        assertThatThrownBy(() -> inventoryService.addInventory(inventory))
                .isInstanceOf(DuplicateInventoryException.class);

        verify(inventoryRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // getLowStockInventory
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getLowStockInventory returns records where quantity <= reorderLevel")
    void getLowStockInventory_returnsLowStockItems() {
        inventory.setQuantity(15); // below reorderLevel of 20
        when(inventoryRepository.findLowStockInventory()).thenReturn(List.of(inventory));

        List<Inventory> result = inventoryService.getLowStockInventory();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantity()).isLessThanOrEqualTo(
                result.get(0).getReorderLevel()
        );
    }

    // ---------------------------------------------------------------
    // deleteInventory
    // ---------------------------------------------------------------

    @Test
    @DisplayName("deleteInventory removes the record from repository")
    void deleteInventory_success() {
        when(inventoryRepository.findById(1)).thenReturn(Optional.of(inventory));
        doNothing().when(inventoryRepository).delete(inventory);

        inventoryService.deleteInventory(1);

        verify(inventoryRepository).delete(inventory);
    }
}
