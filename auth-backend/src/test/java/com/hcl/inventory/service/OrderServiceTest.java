package com.hcl.inventory.service;

import com.hcl.inventory.entity.Inventory;
import com.hcl.inventory.entity.Order;
import com.hcl.inventory.entity.OrderItem;
import com.hcl.inventory.entity.Product;
import com.hcl.inventory.entity.StockMovement;
import com.hcl.inventory.entity.Warehouse;
import com.hcl.inventory.enums.OrderItemStatus;
import com.hcl.inventory.enums.OrderStatus;
import com.hcl.inventory.exception.InvalidOrderException;
import com.hcl.inventory.exception.OrderNotFoundException;
import com.hcl.inventory.repository.InventoryRepository;
import com.hcl.inventory.repository.OrderItemRepository;
import com.hcl.inventory.repository.OrderRepository;
import com.hcl.inventory.repository.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for {@link OrderService}.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private OrderService orderService;

    private Order sampleOrder;
    private Product sampleProduct;
    private Warehouse sampleWarehouse;
    private OrderItem sampleItem;
    private Inventory sampleInventory;

    @BeforeEach
    void setUp() {
        sampleOrder = new Order();
        sampleOrder.setOrderId(1);
        sampleOrder.setOrderNumber("ORD-2026-001");
        sampleOrder.setCustomerName("Alice Smith");
        sampleOrder.setCustomerEmail("alice@example.com");
        sampleOrder.setStatus(OrderStatus.DRAFT);
        sampleOrder.setTotalAmount(new BigDecimal("999.00"));

        sampleProduct = new Product();
        sampleProduct.setProductId(101);
        sampleProduct.setSku("APP-SH-001");
        sampleProduct.setProductName("Classic Shirt");
        sampleProduct.setProductPrice(new BigDecimal("999.00"));

        sampleWarehouse = new Warehouse();
        sampleWarehouse.setWarehouseId(1);
        sampleWarehouse.setWarehouseCode("WH-MUM-01");

        sampleItem = new OrderItem();
        sampleItem.setOrderItemId(10);
        sampleItem.setOrder(sampleOrder);
        sampleItem.setProduct(sampleProduct);
        sampleItem.setWarehouse(sampleWarehouse);
        sampleItem.setQuantity(5);
        sampleItem.setAllocatedQuantity(0);
        sampleItem.setUnitPrice(new BigDecimal("999.00"));
        sampleItem.setStatus(OrderItemStatus.PENDING);

        sampleInventory = new Inventory();
        sampleInventory.setInventoryId(501);
        sampleInventory.setProduct(sampleProduct);
        sampleInventory.setWarehouse(sampleWarehouse);
        sampleInventory.setQuantity(20);
        sampleInventory.setReservedQuantity(2); // available = 18
    }

    // ---------------------------------------------------------------
    // confirmOrder tests
    // ---------------------------------------------------------------

    @Test
    @DisplayName("confirmOrder successfully reserves available stock and marks items ALLOCATED")
    void confirmOrder_success_reservesStock() {
        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(sampleOrder));
        when(orderItemRepository.findByOrderOrderId(1)).thenReturn(new ArrayList<>(List.of(sampleItem)));
        when(inventoryRepository.findByProductProductIdAndWarehouseWarehouseIdWithLock(101, 1))
                .thenReturn(Optional.of(sampleInventory));
        when(orderItemRepository.calculateOrderTotal(1)).thenReturn(new BigDecimal("4995.00"));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order confirmed = orderService.confirmOrder(1);

        assertThat(confirmed.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(sampleInventory.getReservedQuantity()).isEqualTo(7); // 2 + 5
        assertThat(sampleItem.getAllocatedQuantity()).isEqualTo(5);
        assertThat(sampleItem.getStatus()).isEqualTo(OrderItemStatus.ALLOCATED);

        verify(inventoryRepository).save(sampleInventory);
        verify(orderItemRepository).save(sampleItem);
        verify(orderRepository).save(sampleOrder);
    }

    @Test
    @DisplayName("confirmOrder throws exception when order is not in DRAFT status (prevents double reservation)")
    void confirmOrder_notDraft_throwsException() {
        sampleOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> orderService.confirmOrder(1))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("Only DRAFT orders can be confirmed");

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("confirmOrder throws exception when order has no items")
    void confirmOrder_emptyItems_throwsException() {
        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(sampleOrder));
        when(orderItemRepository.findByOrderOrderId(1)).thenReturn(new ArrayList<>());

        assertThatThrownBy(() -> orderService.confirmOrder(1))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("at least one item");
    }

    @Test
    @DisplayName("confirmOrder throws exception when unreserved inventory is insufficient")
    void confirmOrder_insufficientStock_throwsException() {
        sampleInventory.setQuantity(5);
        sampleInventory.setReservedQuantity(3); // available = 2, requested = 5

        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(sampleOrder));
        when(orderItemRepository.findByOrderOrderId(1)).thenReturn(new ArrayList<>(List.of(sampleItem)));
        when(inventoryRepository.findByProductProductIdAndWarehouseWarehouseIdWithLock(101, 1))
                .thenReturn(Optional.of(sampleInventory));

        assertThatThrownBy(() -> orderService.confirmOrder(1))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("Insufficient available stock");

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("confirmOrder throws exception when warehouse is not assigned for item")
    void confirmOrder_missingWarehouse_throwsException() {
        sampleItem.setWarehouse(null);

        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(sampleOrder));
        when(orderItemRepository.findByOrderOrderId(1)).thenReturn(new ArrayList<>(List.of(sampleItem)));

        assertThatThrownBy(() -> orderService.confirmOrder(1))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("Warehouse must be assigned");
    }

    // ---------------------------------------------------------------
    // pickOrder & packOrder tests
    // ---------------------------------------------------------------

    @Test
    @DisplayName("pickOrder transitions CONFIRMED order to PICKED")
    void pickOrder_success() {
        sampleOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order picked = orderService.pickOrder(1);

        assertThat(picked.getStatus()).isEqualTo(OrderStatus.PICKED);
    }

    @Test
    @DisplayName("pickOrder rejects transition from DRAFT")
    void pickOrder_invalidStatus_throwsException() {
        sampleOrder.setStatus(OrderStatus.DRAFT);
        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> orderService.pickOrder(1))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("Only CONFIRMED or PROCESSING orders can be picked");
    }

    @Test
    @DisplayName("packOrder transitions PICKED order to PACKED")
    void packOrder_success() {
        sampleOrder.setStatus(OrderStatus.PICKED);
        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order packed = orderService.packOrder(1);

        assertThat(packed.getStatus()).isEqualTo(OrderStatus.PACKED);
    }

    @Test
    @DisplayName("packOrder rejects transition from DRAFT")
    void packOrder_invalidStatus_throwsException() {
        sampleOrder.setStatus(OrderStatus.DRAFT);
        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> orderService.packOrder(1))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("Only PICKED orders can be packed");
    }

    // ---------------------------------------------------------------
    // shipOrder tests
    // ---------------------------------------------------------------

    @Test
    @DisplayName("shipOrder decrements physical and reserved stock correctly and logs movement")
    void shipOrder_success_decrementsPhysicalAndReservedStock() {
        sampleOrder.setStatus(OrderStatus.PACKED);
        sampleItem.setAllocatedQuantity(5);
        sampleItem.setStatus(OrderItemStatus.ALLOCATED);
        sampleInventory.setQuantity(20);
        sampleInventory.setReservedQuantity(7);

        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(sampleOrder));
        when(orderItemRepository.findByOrderOrderId(1)).thenReturn(List.of(sampleItem));
        when(inventoryRepository.findByProductProductIdAndWarehouseWarehouseIdWithLock(101, 1))
                .thenReturn(Optional.of(sampleInventory));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order shipped = orderService.shipOrder(1);

        assertThat(shipped.getStatus()).isEqualTo(OrderStatus.FULFILLED);
        assertThat(sampleInventory.getQuantity()).isEqualTo(15); // 20 - 5
        assertThat(sampleInventory.getReservedQuantity()).isEqualTo(2); // 7 - 5
        assertThat(sampleItem.getStatus()).isEqualTo(OrderItemStatus.FULFILLED);

        verify(inventoryRepository).save(sampleInventory);
        verify(orderItemRepository).save(sampleItem);
        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("shipOrder throws exception on physical inventory underflow (no silent clamping)")
    void shipOrder_physicalUnderflow_throwsException() {
        sampleOrder.setStatus(OrderStatus.PACKED);
        sampleItem.setAllocatedQuantity(10);
        sampleInventory.setQuantity(5); // physical underflow!
        sampleInventory.setReservedQuantity(10);

        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(sampleOrder));
        when(orderItemRepository.findByOrderOrderId(1)).thenReturn(List.of(sampleItem));
        when(inventoryRepository.findByProductProductIdAndWarehouseWarehouseIdWithLock(101, 1))
                .thenReturn(Optional.of(sampleInventory));

        assertThatThrownBy(() -> orderService.shipOrder(1))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("Physical inventory underflow");

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("shipOrder throws exception on reserved inventory underflow (no silent clamping)")
    void shipOrder_reservedUnderflow_throwsException() {
        sampleOrder.setStatus(OrderStatus.PACKED);
        sampleItem.setAllocatedQuantity(10);
        sampleInventory.setQuantity(20);
        sampleInventory.setReservedQuantity(4); // reserved underflow!

        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(sampleOrder));
        when(orderItemRepository.findByOrderOrderId(1)).thenReturn(List.of(sampleItem));
        when(inventoryRepository.findByProductProductIdAndWarehouseWarehouseIdWithLock(101, 1))
                .thenReturn(Optional.of(sampleInventory));

        assertThatThrownBy(() -> orderService.shipOrder(1))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("Reserved inventory underflow");

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("shipOrder rejects already fulfilled or shipped order")
    void shipOrder_alreadyFulfilled_throwsException() {
        sampleOrder.setStatus(OrderStatus.FULFILLED);
        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> orderService.shipOrder(1))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("already fulfilled/shipped");
    }

    @Test
    @DisplayName("shipOrder rejects shipping a DRAFT order")
    void shipOrder_fromDraft_throwsException() {
        sampleOrder.setStatus(OrderStatus.DRAFT);
        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> orderService.shipOrder(1))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("cannot be shipped from status DRAFT");
    }

    // ---------------------------------------------------------------
    // cancelOrder tests
    // ---------------------------------------------------------------

    @Test
    @DisplayName("cancelOrder from DRAFT does not touch inventory reservations")
    void cancelOrder_fromDraft_noReservationChange() {
        sampleOrder.setStatus(OrderStatus.DRAFT);
        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(sampleOrder));
        when(orderItemRepository.findByOrderOrderId(1)).thenReturn(List.of(sampleItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order cancelled = orderService.cancelOrder(1);

        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(sampleItem.getStatus()).isEqualTo(OrderItemStatus.CANCELLED);
        // sampleItem allocatedQuantity was 0, so inventoryRepository should not be modified
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelOrder from CONFIRMED releases reserved stock exactly once")
    void cancelOrder_fromConfirmed_releasesReservationOnce() {
        sampleOrder.setStatus(OrderStatus.CONFIRMED);
        sampleItem.setAllocatedQuantity(5);
        sampleItem.setStatus(OrderItemStatus.ALLOCATED);
        sampleInventory.setQuantity(20);
        sampleInventory.setReservedQuantity(7);

        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(sampleOrder));
        when(orderItemRepository.findByOrderOrderId(1)).thenReturn(List.of(sampleItem));
        when(inventoryRepository.findByProductProductIdAndWarehouseWarehouseIdWithLock(101, 1))
                .thenReturn(Optional.of(sampleInventory));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order cancelled = orderService.cancelOrder(1);

        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(sampleInventory.getReservedQuantity()).isEqualTo(2); // 7 - 5
        assertThat(sampleItem.getAllocatedQuantity()).isEqualTo(0);
        assertThat(sampleItem.getStatus()).isEqualTo(OrderItemStatus.CANCELLED);

        verify(inventoryRepository, times(1)).save(sampleInventory);
        verify(orderItemRepository).save(sampleItem);
    }

    @Test
    @DisplayName("cancelOrder rejects an already cancelled order")
    void cancelOrder_alreadyCancelled_throwsException() {
        sampleOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> orderService.cancelOrder(1))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("Order is already cancelled");
    }

    @Test
    @DisplayName("cancelOrder rejects cancelling a fulfilled order")
    void cancelOrder_alreadyFulfilled_throwsException() {
        sampleOrder.setStatus(OrderStatus.FULFILLED);
        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> orderService.cancelOrder(1))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("fulfilled order cannot be cancelled");
    }

    // ---------------------------------------------------------------
    // Concurrent allocation simulation
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Simulate concurrent orders allocating inventory: second order fails when unreserved stock exhausted")
    void concurrentAllocation_simulation_test() {
        // Stock: quantity 10, reserved 0 => available 10
        sampleInventory.setQuantity(10);
        sampleInventory.setReservedQuantity(0);

        // Order 1 requests 8 units
        Order order1 = new Order();
        order1.setOrderId(1);
        order1.setStatus(OrderStatus.DRAFT);
        OrderItem item1 = new OrderItem();
        item1.setOrderItemId(11);
        item1.setOrder(order1);
        item1.setProduct(sampleProduct);
        item1.setWarehouse(sampleWarehouse);
        item1.setQuantity(8);

        // Order 2 requests 5 units (total 13 > 10)
        Order order2 = new Order();
        order2.setOrderId(2);
        order2.setStatus(OrderStatus.DRAFT);
        OrderItem item2 = new OrderItem();
        item2.setOrderItemId(12);
        item2.setOrder(order2);
        item2.setProduct(sampleProduct);
        item2.setWarehouse(sampleWarehouse);
        item2.setQuantity(5);

        when(orderRepository.findByIdWithLock(1)).thenReturn(Optional.of(order1));
        when(orderItemRepository.findByOrderOrderId(1)).thenReturn(new ArrayList<>(List.of(item1)));
        when(inventoryRepository.findByProductProductIdAndWarehouseWarehouseIdWithLock(101, 1))
                .thenReturn(Optional.of(sampleInventory));
        when(orderRepository.save(order1)).thenReturn(order1);

        // First order confirms and reserves 8 units
        Order confirmed1 = orderService.confirmOrder(1);
        assertThat(confirmed1.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(sampleInventory.getReservedQuantity()).isEqualTo(8);

        // Now Order 2 tries to confirm - available is now 10 - 8 = 2, but requested is 5
        when(orderRepository.findByIdWithLock(2)).thenReturn(Optional.of(order2));
        when(orderItemRepository.findByOrderOrderId(2)).thenReturn(new ArrayList<>(List.of(item2)));

        assertThatThrownBy(() -> orderService.confirmOrder(2))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("Insufficient available stock");

        // Order 2 status remains DRAFT, reserved quantity remains 8
        assertThat(order2.getStatus()).isEqualTo(OrderStatus.DRAFT);
        assertThat(sampleInventory.getReservedQuantity()).isEqualTo(8);
    }
}
