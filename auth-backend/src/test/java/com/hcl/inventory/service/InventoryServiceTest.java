package com.hcl.inventory.service;

import com.hcl.inventory.dto.StockTransferRequest;
import com.hcl.inventory.entity.Inventory;
import com.hcl.inventory.entity.Product;
import com.hcl.inventory.entity.StockMovement;
import com.hcl.inventory.entity.Warehouse;
import com.hcl.inventory.enums.InventoryStatus;
import com.hcl.inventory.exception.DuplicateInventoryException;
import com.hcl.inventory.exception.InventoryNotFoundException;
import com.hcl.inventory.exception.InvalidInventoryQuantityException;
import com.hcl.inventory.exception.ProductNotFoundException;
import com.hcl.inventory.exception.WarehouseNotFoundException;
import com.hcl.inventory.repository.InventoryRepository;
import com.hcl.inventory.repository.ProductRepository;
import com.hcl.inventory.repository.StockMovementRepository;
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
import java.util.Map;
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

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product product;
    private Warehouse warehouse;
    private Warehouse destinationWarehouse;
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
        warehouse.setWarehouseCode("WH-MUM-01");
        warehouse.setWarehouseName("Mumbai Hub");

        destinationWarehouse = new Warehouse();
        destinationWarehouse.setWarehouseId(6);
        destinationWarehouse.setWarehouseCode("WH-DEL-02");
        destinationWarehouse.setWarehouseName("Delhi Hub");

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

    // ---------------------------------------------------------------
    // transferStock (Task 2)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("transferStock successfully transfers unreserved stock and records movements")
    void transferStock_success() {
        StockTransferRequest request = new StockTransferRequest(10, 5, 6, 20);
        request.setReferenceNumber("TRF-TEST-001");
        request.setReason("Rebalancing stock");
        request.setPerformedBy("admin");

        Inventory destinationInventory = new Inventory();
        destinationInventory.setInventoryId(2);
        destinationInventory.setProduct(product);
        destinationInventory.setWarehouse(destinationWarehouse);
        destinationInventory.setQuantity(30);
        destinationInventory.setReservedQuantity(0);

        when(productRepository.findById(10)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(5)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.findById(6)).thenReturn(Optional.of(destinationWarehouse));
        when(inventoryRepository.findByProductProductIdAndWarehouseWarehouseIdWithLock(10, 5))
                .thenReturn(Optional.of(inventory)); // quantity 100, reserved 10 => available 90
        when(inventoryRepository.findByProductProductIdAndWarehouseWarehouseIdWithLock(10, 6))
                .thenReturn(Optional.of(destinationInventory));

        Map<String, Object> result = inventoryService.transferStock(request);

        assertThat(result.get("status")).isEqualTo("SUCCESS");
        assertThat(result.get("transferredQuantity")).isEqualTo(20);
        assertThat(result.get("sourceRemainingQuantity")).isEqualTo(80);
        assertThat(result.get("targetNewQuantity")).isEqualTo(50);

        // Verify source inventory deducted
        assertThat(inventory.getQuantity()).isEqualTo(80);
        // Verify destination inventory incremented
        assertThat(destinationInventory.getQuantity()).isEqualTo(50);

        verify(inventoryRepository, times(2)).save(any(Inventory.class));
        verify(stockMovementRepository, times(2)).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("transferStock rejects null request")
    void transferStock_nullRequest_throwsException() {
        assertThatThrownBy(() -> inventoryService.transferStock(null))
                .isInstanceOf(InvalidInventoryQuantityException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("transferStock rejects missing product ID")
    void transferStock_missingProductId_throwsException() {
        StockTransferRequest request = new StockTransferRequest(null, 5, 6, 10);

        assertThatThrownBy(() -> inventoryService.transferStock(request))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product ID is required");
    }

    @Test
    @DisplayName("transferStock rejects missing source warehouse ID")
    void transferStock_missingSourceWarehouseId_throwsException() {
        StockTransferRequest request = new StockTransferRequest(10, null, 6, 10);

        assertThatThrownBy(() -> inventoryService.transferStock(request))
                .isInstanceOf(WarehouseNotFoundException.class)
                .hasMessageContaining("Source warehouse ID is required");
    }

    @Test
    @DisplayName("transferStock rejects missing destination warehouse ID")
    void transferStock_missingDestinationWarehouseId_throwsException() {
        StockTransferRequest request = new StockTransferRequest(10, 5, null, 10);

        assertThatThrownBy(() -> inventoryService.transferStock(request))
                .isInstanceOf(WarehouseNotFoundException.class)
                .hasMessageContaining("Destination warehouse ID is required");
    }

    @Test
    @DisplayName("transferStock rejects zero or negative quantity")
    void transferStock_zeroOrNegativeQuantity_throwsException() {
        StockTransferRequest zeroReq = new StockTransferRequest(10, 5, 6, 0);
        assertThatThrownBy(() -> inventoryService.transferStock(zeroReq))
                .isInstanceOf(InvalidInventoryQuantityException.class)
                .hasMessageContaining("greater than zero");

        StockTransferRequest negReq = new StockTransferRequest(10, 5, 6, -5);
        assertThatThrownBy(() -> inventoryService.transferStock(negReq))
                .isInstanceOf(InvalidInventoryQuantityException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    @DisplayName("transferStock rejects identical source and destination warehouses")
    void transferStock_identicalWarehouses_throwsException() {
        StockTransferRequest request = new StockTransferRequest(10, 5, 5, 10);

        assertThatThrownBy(() -> inventoryService.transferStock(request))
                .isInstanceOf(InvalidInventoryQuantityException.class)
                .hasMessageContaining("Source and destination warehouses cannot be the same");
    }

    @Test
    @DisplayName("transferStock throws exception when insufficient unreserved stock")
    void transferStock_insufficientStock_throwsException() {
        // available = quantity(100) - reserved(10) = 90. Requesting 95 should fail.
        StockTransferRequest request = new StockTransferRequest(10, 5, 6, 95);

        when(productRepository.findById(10)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(5)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.findById(6)).thenReturn(Optional.of(destinationWarehouse));
        when(inventoryRepository.findByProductProductIdAndWarehouseWarehouseIdWithLock(10, 5))
                .thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryService.transferStock(request))
                .isInstanceOf(InvalidInventoryQuantityException.class)
                .hasMessageContaining("Insufficient unreserved stock in source warehouse");

        // Verify stock was not deducted and no movements were saved
        verify(inventoryRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    @DisplayName("transferStock throws exception when source inventory not found")
    void transferStock_sourceInventoryNotFound_throwsException() {
        StockTransferRequest request = new StockTransferRequest(10, 5, 6, 10);

        when(productRepository.findById(10)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(5)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.findById(6)).thenReturn(Optional.of(destinationWarehouse));
        when(inventoryRepository.findByProductProductIdAndWarehouseWarehouseIdWithLock(10, 5))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.transferStock(request))
                .isInstanceOf(InventoryNotFoundException.class)
                .hasMessageContaining("source warehouse not found");

        verify(stockMovementRepository, never()).save(any());
    }
}
