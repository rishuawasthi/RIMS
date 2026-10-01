package com.hcl.inventory.service;

import com.hcl.inventory.entity.Order;
import com.hcl.inventory.enums.OrderStatus;
import com.hcl.inventory.exception.DuplicateOrderException;
import com.hcl.inventory.exception.InvalidOrderException;
import com.hcl.inventory.exception.OrderNotFoundException;
import com.hcl.inventory.repository.OrderItemRepository;
import com.hcl.inventory.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Autowired
    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
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

        String orderNumber =
                normalizeOrderNumber(order.getOrderNumber());

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

        Order existingOrder =
                getOrderById(orderId);

        if (existingOrder.getStatus()
                != OrderStatus.DRAFT) {

            throw new InvalidOrderException(
                    "Only DRAFT orders can be updated"
            );
        }

        String orderNumber =
                normalizeOrderNumber(
                        updatedOrder.getOrderNumber()
                );

        if (orderRepository
                .existsByOrderNumberAndOrderIdNot(
                        orderNumber,
                        orderId
                )) {

            throw new DuplicateOrderException(
                    "Order with number "
                            + orderNumber
                            + " already exists"
            );
        }

        existingOrder.setOrderNumber(orderNumber);
        existingOrder.setCustomerName(
                updatedOrder.getCustomerName()
        );
        existingOrder.setCustomerEmail(
                updatedOrder.getCustomerEmail()
        );
        existingOrder.setCustomerPhone(
                updatedOrder.getCustomerPhone()
        );
        existingOrder.setShippingAddress(
                updatedOrder.getShippingAddress()
        );
        existingOrder.setCity(
                updatedOrder.getCity()
        );
        existingOrder.setState(
                updatedOrder.getState()
        );
        existingOrder.setPincode(
                updatedOrder.getPincode()
        );
        existingOrder.setNotes(
                updatedOrder.getNotes()
        );

        recalculateTotal(existingOrder);

        return orderRepository.save(existingOrder);
    }

    @Transactional
    public Order confirmOrder(Integer orderId) {

        Order order = getOrderById(orderId);

        if (order.getStatus() != OrderStatus.DRAFT) {

            throw new InvalidOrderException(
                    "Only DRAFT orders can be confirmed"
            );
        }

        long itemCount =
                orderItemRepository
                        .findByOrderOrderId(orderId)
                        .size();

        if (itemCount == 0) {

            throw new InvalidOrderException(
                    "An order must contain at least one item before confirmation"
            );
        }

        recalculateTotal(order);

        order.setStatus(OrderStatus.CONFIRMED);

        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Integer orderId) {

        Order order = getOrderById(orderId);

        if (order.getStatus()
                == OrderStatus.FULFILLED) {

            throw new InvalidOrderException(
                    "A fulfilled order cannot be cancelled"
            );
        }

        if (order.getStatus()
                == OrderStatus.CANCELLED) {

            throw new InvalidOrderException(
                    "Order is already cancelled"
            );
        }

        order.setStatus(OrderStatus.CANCELLED);

        return orderRepository.save(order);
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

            throw new InvalidOrderException(
                    "Only DRAFT or CANCELLED orders can be deleted"
            );
        }

        orderRepository.delete(order);
    }

    private void recalculateTotal(Order order) {

        BigDecimal total =
                orderItemRepository
                        .calculateOrderTotal(
                                order.getOrderId()
                        );

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

        return orderNumber
                .trim()
                .toUpperCase();
    }
}