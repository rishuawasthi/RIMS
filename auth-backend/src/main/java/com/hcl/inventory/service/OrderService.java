package com.hcl.inventory.service;

import com.hcl.inventory.entity.Inventory;
import com.hcl.inventory.entity.Order;
import com.hcl.inventory.entity.OrderItem;
import com.hcl.inventory.entity.StockMovement;
import com.hcl.inventory.enums.MovementType;
import com.hcl.inventory.enums.OrderItemStatus;
import com.hcl.inventory.enums.OrderStatus;
import com.hcl.inventory.exception.DuplicateOrderException;
import com.hcl.inventory.exception.InvalidOrderException;
import com.hcl.inventory.exception.OrderNotFoundException;
import com.hcl.inventory.repository.InventoryRepository;
import com.hcl.inventory.repository.OrderItemRepository;
import com.hcl.inventory.repository.OrderRepository;
import com.hcl.inventory.repository.StockMovementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;

    @Autowired
    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            @Autowired(required = false) InventoryRepository inventoryRepository,
            @Autowired(required = false) StockMovementRepository stockMovementRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository
    ) {
        this(orderRepository, orderItemRepository, null, null);
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order with id "
                                        + orderId
                                        + " not found"
                        )
                );
    }

    @Transactional
    public Order addOrder(Order order) {
        String orderNumber = normalizeOrderNumber(order.getOrderNumber());

        if (orderRepository.existsByOrderNumber(orderNumber)) {
            throw new DuplicateOrderException(
                    "Order with number "
                            + orderNumber
                            + " already exists"
            );
        }

        order.setOrderNumber(orderNumber);
        order.setStatus(OrderStatus.DRAFT);
        order.setTotalAmount(BigDecimal.ZERO);

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrder(
            Integer orderId,
            Order updatedOrder
    ) {
        Order existingOrder = getOrderById(orderId);

        if (existingOrder.getStatus() != OrderStatus.DRAFT) {
            throw new InvalidOrderException("Only DRAFT orders can be updated");
        }

        String orderNumber = normalizeOrderNumber(updatedOrder.getOrderNumber());

        if (orderRepository.existsByOrderNumberAndOrderIdNot(orderNumber, orderId)) {
            throw new DuplicateOrderException(
                    "Order with number "
                            + orderNumber
                            + " already exists"
            );
        }

        existingOrder.setOrderNumber(orderNumber);
        existingOrder.setCustomerName(updatedOrder.getCustomerName());
        existingOrder.setCustomerEmail(updatedOrder.getCustomerEmail());
        existingOrder.setCustomerPhone(updatedOrder.getCustomerPhone());
        existingOrder.setShippingAddress(updatedOrder.getShippingAddress());
        existingOrder.setCity(updatedOrder.getCity());
        existingOrder.setState(updatedOrder.getState());
        existingOrder.setPincode(updatedOrder.getPincode());
        existingOrder.setNotes(updatedOrder.getNotes());

        recalculateTotal(existingOrder);

        return orderRepository.save(existingOrder);
    }

    @Transactional
    public Order confirmOrder(Integer orderId) {
        Order order = getOrderByIdWithLock(orderId);

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new InvalidOrderException("Only DRAFT orders can be confirmed");
        }

        List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);

        if (items.isEmpty()) {
            throw new InvalidOrderException("An order must contain at least one item before confirmation");
        }

        // Sort items deterministically to avoid deadlocks during concurrent multi-item allocations
        items.sort((a, b) -> {
            int pComp = Integer.compare(
                    a.getProduct() != null && a.getProduct().getProductId() != null ? a.getProduct().getProductId() : 0,
                    b.getProduct() != null && b.getProduct().getProductId() != null ? b.getProduct().getProductId() : 0
            );
            if (pComp != 0) return pComp;
            return Integer.compare(
                    a.getWarehouse() != null && a.getWarehouse().getWarehouseId() != null ? a.getWarehouse().getWarehouseId() : 0,
                    b.getWarehouse() != null && b.getWarehouse().getWarehouseId() != null ? b.getWarehouse().getWarehouseId() : 0
            );
        });

        // Execute atomic stock reservation for all order items with pessimistic locking
        if (inventoryRepository != null) {
            for (OrderItem item : items) {
                if (item.getWarehouse() == null) {
                    throw new InvalidOrderException("Warehouse must be assigned for product "
                            + (item.getProduct() != null ? item.getProduct().getSku() : "unknown")
                            + " to allocate inventory");
                }

                Inventory inv = inventoryRepository.findByProductProductIdAndWarehouseWarehouseIdWithLock(
                        item.getProduct().getProductId(), item.getWarehouse().getWarehouseId())
                        .orElseThrow(() -> new InvalidOrderException("No inventory record found for product "
                                + item.getProduct().getSku() + " in warehouse " + item.getWarehouse().getWarehouseCode()));

                int reserved = inv.getReservedQuantity() != null ? inv.getReservedQuantity() : 0;
                int available = inv.getQuantity() - reserved;
                if (available < item.getQuantity()) {
                    throw new InvalidOrderException("Insufficient available stock for SKU "
                            + item.getProduct().getSku() + " in warehouse "
                            + item.getWarehouse().getWarehouseCode() + ". Available: " + available
                            + ", Requested: " + item.getQuantity());
                }

                inv.setReservedQuantity(reserved + item.getQuantity());
                inventoryRepository.save(inv);

                item.setAllocatedQuantity(item.getQuantity());
                item.setStatus(OrderItemStatus.ALLOCATED);
                orderItemRepository.save(item);
            }
        }

        recalculateTotal(order);
        order.setStatus(OrderStatus.CONFIRMED);

        return orderRepository.save(order);
    }

    @Transactional
    public Order pickOrder(Integer orderId) {
        Order order = getOrderByIdWithLock(orderId);

        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.PROCESSING) {
            throw new InvalidOrderException("Only CONFIRMED or PROCESSING orders can be picked");
        }

        order.setStatus(OrderStatus.PICKED);
        return orderRepository.save(order);
    }

    @Transactional
    public Order packOrder(Integer orderId) {
        Order order = getOrderByIdWithLock(orderId);

        if (order.getStatus() != OrderStatus.PICKED && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderException("Only PICKED orders can be packed");
        }

        order.setStatus(OrderStatus.PACKED);
        return orderRepository.save(order);
    }

    @Transactional
    public Order shipOrder(Integer orderId) {
        Order order = getOrderByIdWithLock(orderId);

        if (order.getStatus() == OrderStatus.FULFILLED || order.getStatus() == OrderStatus.SHIPPED) {
            throw new InvalidOrderException("Order is already fulfilled/shipped");
        }

        if (order.getStatus() == OrderStatus.DRAFT || order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderException("Order cannot be shipped from status " + order.getStatus());
        }

        if (order.getStatus() != OrderStatus.PACKED && order.getStatus() != OrderStatus.PICKED && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderException("Order must be confirmed, picked, or packed before shipping");
        }

        List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);

        // Deduct inventory physical quantity and reserved quantity atomically, log stock movement
        if (inventoryRepository != null) {
            for (OrderItem item : items) {
                if (item.getWarehouse() == null) {
                    throw new InvalidOrderException("Warehouse must be assigned for product to fulfill shipment");
                }

                Inventory inv = inventoryRepository.findByProductProductIdAndWarehouseWarehouseIdWithLock(
                        item.getProduct().getProductId(), item.getWarehouse().getWarehouseId())
                        .orElseThrow(() -> new InvalidOrderException("No inventory record found for product "
                                + (item.getProduct() != null ? item.getProduct().getSku() : "unknown")
                                + " in warehouse " + item.getWarehouse().getWarehouseCode()));

                int allocated = item.getAllocatedQuantity() != null && item.getAllocatedQuantity() > 0
                        ? item.getAllocatedQuantity()
                        : item.getQuantity();

                // Explicit invariant checks replacing silent clamping
                if (inv.getQuantity() < allocated) {
                    throw new InvalidOrderException("Physical inventory underflow: warehouse only has "
                            + inv.getQuantity() + " units for SKU " + item.getProduct().getSku()
                            + ", cannot ship " + allocated);
                }

                int currentReserved = inv.getReservedQuantity() != null ? inv.getReservedQuantity() : 0;
                if (currentReserved < allocated) {
                    throw new InvalidOrderException("Reserved inventory underflow: warehouse only has "
                            + currentReserved + " reserved units for SKU " + item.getProduct().getSku()
                            + ", cannot ship " + allocated);
                }

                inv.setQuantity(inv.getQuantity() - allocated);
                inv.setReservedQuantity(currentReserved - allocated);
                inventoryRepository.save(inv);

                if (stockMovementRepository != null) {
                    stockMovementRepository.save(new StockMovement(
                            item.getProduct(), item.getWarehouse(),
                            MovementType.OUTBOUND_SALE, allocated,
                            order.getOrderNumber(), "Order Shipment Fulfillment", "FULFILLMENT_SYSTEM"
                    ));
                }

                item.setStatus(OrderItemStatus.FULFILLED);
                orderItemRepository.save(item);
            }
        }

        order.setStatus(OrderStatus.FULFILLED);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Integer orderId) {
        Order order = getOrderByIdWithLock(orderId);

        if (order.getStatus() == OrderStatus.FULFILLED || order.getStatus() == OrderStatus.SHIPPED) {
            throw new InvalidOrderException("A fulfilled order cannot be cancelled");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderException("Order is already cancelled");
        }

        // Release reserved inventory exactly once
        List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);
        if (inventoryRepository != null) {
            for (OrderItem item : items) {
                if (item.getAllocatedQuantity() != null && item.getAllocatedQuantity() > 0 && item.getWarehouse() != null) {
                    Inventory inv = inventoryRepository.findByProductProductIdAndWarehouseWarehouseIdWithLock(
                            item.getProduct().getProductId(), item.getWarehouse().getWarehouseId())
                            .orElseThrow(() -> new InvalidOrderException("No inventory record found to release reservation for SKU "
                                    + (item.getProduct() != null ? item.getProduct().getSku() : "unknown")));

                    int allocated = item.getAllocatedQuantity();
                    int currentReserved = inv.getReservedQuantity() != null ? inv.getReservedQuantity() : 0;

                    if (currentReserved < allocated) {
                        throw new InvalidOrderException("Cannot cancel order: reserved quantity on inventory ("
                                + currentReserved + ") is less than allocated quantity (" + allocated + ")");
                    }

                    inv.setReservedQuantity(currentReserved - allocated);
                    inventoryRepository.save(inv);
                    item.setAllocatedQuantity(0);
                }

                item.setStatus(OrderItemStatus.CANCELLED);
                orderItemRepository.save(item);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    private Order getOrderByIdWithLock(Integer orderId) {
        return orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order with id "
                                        + orderId
                                        + " not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Order> searchByOrderNumber(
            String orderNumber
    ) {
        return orderRepository
                .findByOrderNumberContainingIgnoreCase(
                        orderNumber
                );
    }

    @Transactional(readOnly = true)
    public List<Order> searchByCustomerName(
            String customerName
    ) {
        return orderRepository
                .findByCustomerNameContainingIgnoreCase(
                        customerName
                );
    }

    @Transactional(readOnly = true)
    public List<Order> getByCustomerEmail(
            String customerEmail
    ) {
        return orderRepository
                .findByCustomerEmailIgnoreCase(
                        customerEmail
                );
    }

    @Transactional(readOnly = true)
    public List<Order> getByStatus(
            OrderStatus status
    ) {
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public void deleteOrder(Integer orderId) {
        Order order = getOrderById(orderId);

        if (order.getStatus() != OrderStatus.DRAFT
                && order.getStatus() != OrderStatus.CANCELLED) {
            throw new InvalidOrderException("Only DRAFT or CANCELLED orders can be deleted");
        }

        orderRepository.delete(order);
    }

    private void recalculateTotal(Order order) {
        BigDecimal total = orderItemRepository.calculateOrderTotal(order.getOrderId());
        if (total == null) {
            total = BigDecimal.ZERO;
        }
        order.setTotalAmount(total);
    }

    private String normalizeOrderNumber(
            String orderNumber
    ) {
        if (orderNumber == null) {
            return null;
        }
        return orderNumber.trim().toUpperCase();
    }
}
