package com.hcl.inventory.service;

import com.hcl.inventory.entity.Order;
import com.hcl.inventory.entity.OrderItem;
import com.hcl.inventory.entity.Product;
import com.hcl.inventory.entity.Warehouse;
import com.hcl.inventory.enums.OrderItemStatus;
import com.hcl.inventory.enums.OrderStatus;
import com.hcl.inventory.exception.DuplicateOrderItemException;
import com.hcl.inventory.exception.InvalidOrderItemException;
import com.hcl.inventory.exception.OrderItemNotFoundException;
import com.hcl.inventory.exception.OrderNotFoundException;
import com.hcl.inventory.exception.ProductNotFoundException;
import com.hcl.inventory.exception.WarehouseNotFoundException;
import com.hcl.inventory.repository.OrderItemRepository;
import com.hcl.inventory.repository.OrderRepository;
import com.hcl.inventory.repository.ProductRepository;
import com.hcl.inventory.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    @Autowired
    public OrderItemService(
            OrderItemRepository orderItemRepository,
            OrderRepository orderRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository
    ) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getAllOrderItems() {

        return orderItemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public OrderItem getOrderItemById(
            Integer orderItemId
    ) {

        return orderItemRepository.findById(orderItemId)
                .orElseThrow(() ->
                        new OrderItemNotFoundException(
                                "Order item with id "
                                        + orderItemId
                                        + " not found"
                        )
                );
    }

    @Transactional
    public OrderItem addOrderItem(
            OrderItem orderItem
    ) {

        if (orderItem.getOrder() == null
                || orderItem.getOrder().getOrderId() == null) {

            throw new OrderNotFoundException(
                    "Order id is required"
            );
        }

        if (orderItem.getProduct() == null
                || orderItem.getProduct().getProductId() == null) {

            throw new ProductNotFoundException(
                    "Product id is required"
            );
        }

        Integer orderId =
                orderItem.getOrder().getOrderId();

        Integer productId =
                orderItem.getProduct().getProductId();

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new OrderNotFoundException(
                                        "Order with id "
                                                + orderId
                                                + " not found"
                                )
                        );

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() ->
                                new ProductNotFoundException(
                                        "Product with id "
                                                + productId
                                                + " not found"
                                )
                        );

        validateOrderIsEditable(order);

        if (orderItemRepository
                .existsByOrderOrderIdAndProductProductId(
                        orderId,
                        productId
                )) {

            throw new DuplicateOrderItemException(
                    "Product "
                            + productId
                            + " already exists in order "
                            + orderId
            );
        }

        validateQuantity(orderItem.getQuantity());

        Warehouse warehouse = null;

        if (orderItem.getWarehouse() != null
                && orderItem.getWarehouse().getWarehouseId() != null) {

            warehouse =
                    warehouseRepository.findById(
                                    orderItem.getWarehouse().getWarehouseId()
                            )
                            .orElseThrow(() ->
                                    new WarehouseNotFoundException(
                                            "Warehouse with id "
                                                    + orderItem
                                                    .getWarehouse()
                                                    .getWarehouseId()
                                                    + " not found"
                                    )
                            );
        }

        BigDecimal unitPrice =
                orderItem.getUnitPrice();

        if (unitPrice == null) {
            unitPrice = product.getProductPrice();
        }

        if (unitPrice == null
                || unitPrice.signum() <= 0) {

            throw new InvalidOrderItemException(
                    "Unit price must be greater than 0"
            );
        }

        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setWarehouse(warehouse);
        orderItem.setUnitPrice(unitPrice);
        orderItem.setAllocatedQuantity(0);
        orderItem.setStatus(OrderItemStatus.PENDING);

        OrderItem savedItem =
                orderItemRepository.save(orderItem);

        recalculateOrderTotal(order);

        return savedItem;
    }

    @Transactional
    public OrderItem updateOrderItem(
            Integer orderItemId,
            OrderItem updatedItem
    ) {

        OrderItem existingItem =
                getOrderItemById(orderItemId);

        Order order =
                existingItem.getOrder();

        validateOrderIsEditable(order);

        if (updatedItem.getProduct() == null
                || updatedItem.getProduct().getProductId() == null) {

            throw new ProductNotFoundException(
                    "Product id is required"
            );
        }

        Integer productId =
                updatedItem
                        .getProduct()
                        .getProductId();

        if (orderItemRepository
                .existsByOrderOrderIdAndProductProductIdAndOrderItemIdNot(
                        order.getOrderId(),
                        productId,
                        orderItemId
                )) {

            throw new DuplicateOrderItemException(
                    "Product "
                            + productId
                            + " already exists in order "
                            + order.getOrderId()
            );
        }

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() ->
                                new ProductNotFoundException(
                                        "Product with id "
                                                + productId
                                                + " not found"
                                )
                        );

        validateQuantity(updatedItem.getQuantity());

        Warehouse warehouse = null;

        if (updatedItem.getWarehouse() != null
                && updatedItem.getWarehouse().getWarehouseId() != null) {

            Integer warehouseId =
                    updatedItem
                            .getWarehouse()
                            .getWarehouseId();

            warehouse =
                    warehouseRepository.findById(
                                    warehouseId
                            )
                            .orElseThrow(() ->
                                    new WarehouseNotFoundException(
                                            "Warehouse with id "
                                                    + warehouseId
                                                    + " not found"
                                    )
                            );
        }

        BigDecimal unitPrice =
                updatedItem.getUnitPrice();

        if (unitPrice == null) {
            unitPrice = product.getProductPrice();
        }

        if (unitPrice == null
                || unitPrice.signum() <= 0) {

            throw new InvalidOrderItemException(
                    "Unit price must be greater than 0"
            );
        }

        if (existingItem.getAllocatedQuantity() > 0) {

            throw new InvalidOrderItemException(
                    "An allocated order item cannot be modified"
            );
        }

        existingItem.setProduct(product);
        existingItem.setWarehouse(warehouse);
        existingItem.setQuantity(
                updatedItem.getQuantity()
        );
        existingItem.setUnitPrice(unitPrice);
        existingItem.setAllocatedQuantity(0);
        existingItem.setStatus(OrderItemStatus.PENDING);

        OrderItem savedItem =
                orderItemRepository.save(existingItem);

        recalculateOrderTotal(order);

        return savedItem;
    }

    @Transactional
    public void deleteOrderItem(
            Integer orderItemId
    ) {

        OrderItem orderItem =
                getOrderItemById(orderItemId);

        validateOrderIsEditable(
                orderItem.getOrder()
        );

        if (orderItem.getAllocatedQuantity() > 0) {

            throw new InvalidOrderItemException(
                    "An allocated order item cannot be deleted"
            );
        }

        Order order = orderItem.getOrder();

        orderItemRepository.delete(orderItem);

        recalculateOrderTotal(order);
    }

    @Transactional
    public OrderItem cancelOrderItem(
            Integer orderItemId
    ) {

        OrderItem orderItem =
                getOrderItemById(orderItemId);

        Order order =
                orderItem.getOrder();

        if (order.getStatus() == OrderStatus.FULFILLED) {

            throw new InvalidOrderItemException(
                    "An item from a fulfilled order cannot be cancelled"
            );
        }

        if (orderItem.getStatus()
                == OrderItemStatus.CANCELLED) {

            throw new InvalidOrderItemException(
                    "Order item is already cancelled"
            );
        }

        if (orderItem.getAllocatedQuantity() > 0) {

            throw new InvalidOrderItemException(
                    "An allocated order item cannot be cancelled before stock handling"
            );
        }

        orderItem.setStatus(
                OrderItemStatus.CANCELLED
        );

        return orderItemRepository.save(orderItem);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getItemsByOrder(
            Integer orderId
    ) {

        orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order with id "
                                        + orderId
                                        + " not found"
                        )
                );

        return orderItemRepository
                .findByOrderOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getItemsByProduct(
            Integer productId
    ) {

        productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product with id "
                                        + productId
                                        + " not found"
                        )
                );

        return orderItemRepository
                .findByProductProductId(productId);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getItemsByWarehouse(
            Integer warehouseId
    ) {

        warehouseRepository.findById(warehouseId)
                .orElseThrow(() ->
                        new WarehouseNotFoundException(
                                "Warehouse with id "
                                        + warehouseId
                                        + " not found"
                        )
                );

        return orderItemRepository
                .findByWarehouseWarehouseId(
                        warehouseId
                );
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getItemsByStatus(
            OrderItemStatus status
    ) {

        return orderItemRepository.findByStatus(status);
    }

    private void validateOrderIsEditable(
            Order order
    ) {

        if (order.getStatus() != OrderStatus.DRAFT) {

            throw new InvalidOrderItemException(
                    "Order items can only be changed while the order is DRAFT"
            );
        }
    }

    private void validateQuantity(
            Integer quantity
    ) {

        if (quantity == null || quantity <= 0) {

            throw new InvalidOrderItemException(
                    "Order item quantity must be greater than 0"
            );
        }
    }

    private void recalculateOrderTotal(
            Order order
    ) {

        BigDecimal total =
                orderItemRepository
                        .calculateOrderTotal(
                                order.getOrderId()
                        );

        if (total == null) {
            total = BigDecimal.ZERO;
        }

        order.setTotalAmount(total);

        orderRepository.save(order);
    }
}