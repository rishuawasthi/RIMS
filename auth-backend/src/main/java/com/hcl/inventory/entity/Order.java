package com.hcl.inventory.entity;

import com.hcl.inventory.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    @NotBlank(message = "Order number is required")
    @Size(
            max = 40,
            message = "Order number cannot exceed 40 characters"
    )
    @Pattern(
            regexp = "^[A-Za-z0-9-]+$",
            message = "Order number can contain only letters, numbers and hyphens"
    )
    @Column(nullable = false, unique = true, length = 40)
    private String orderNumber;

    @NotBlank(message = "Customer name is required")
    @Size(
            max = 150,
            message = "Customer name cannot exceed 150 characters"
    )
    @Column(nullable = false, length = 150)
    private String customerName;

    @Email(message = "Customer email must be valid")
    @Size(
            max = 150,
            message = "Customer email cannot exceed 150 characters"
    )
    @Column(length = 150)
    private String customerEmail;

    @Pattern(
            regexp = "^[0-9]{10,15}$",
            message = "Customer phone must contain 10 to 15 digits"
    )
    @Column(length = 15)
    private String customerPhone;

    @NotBlank(message = "Shipping address is required")
    @Size(
            max = 250,
            message = "Shipping address cannot exceed 250 characters"
    )
    @Column(nullable = false, length = 250)
    private String shippingAddress;

    @NotBlank(message = "City is required")
    @Size(
            max = 100,
            message = "City cannot exceed 100 characters"
    )
    @Column(nullable = false, length = 100)
    private String city;

    @NotBlank(message = "State is required")
    @Size(
            max = 100,
            message = "State cannot exceed 100 characters"
    )
    @Column(nullable = false, length = 100)
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "Pincode must contain exactly 6 digits"
    )
    @Column(nullable = false, length = 6)
    private String pincode;

    @NotNull(message = "Order status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.DRAFT;

    @NotNull(message = "Total amount is required")
    @DecimalMin(
            value = "0.00",
            message = "Total amount cannot be negative"
    )
    @Digits(
            integer = 12,
            fraction = 2,
            message = "Total amount must have at most 2 decimal places"
    )
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Size(
            max = 500,
            message = "Order notes cannot exceed 500 characters"
    )
    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        orderDate = now;
        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = OrderStatus.DRAFT;
        }

        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();

        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}