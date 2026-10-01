package com.hcl.inventory.entity;

import com.hcl.inventory.enums.SupplierStatus;
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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "supplier")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer supplierId;

    @NotBlank(message = "Supplier code is required")
    @Size(
            max = 30,
            message = "Supplier code cannot exceed 30 characters"
    )
    @Pattern(
            regexp = "^[A-Za-z0-9-]+$",
            message = "Supplier code can contain only letters, numbers and hyphens"
    )
    @Column(
            nullable = false,
            unique = true,
            length = 30
    )
    private String supplierCode;

    @NotBlank(message = "Supplier name is required")
    @Size(
            max = 150,
            message = "Supplier name cannot exceed 150 characters"
    )
    @Column(
            nullable = false,
            length = 150
    )
    private String supplierName;

    @NotBlank(message = "Contact person is required")
    @Size(
            max = 100,
            message = "Contact person cannot exceed 100 characters"
    )
    @Column(
            nullable = false,
            length = 100
    )
    private String contactPerson;

    @NotBlank(message = "Supplier email is required")
    @Email(message = "Supplier email must be valid")
    @Size(
            max = 150,
            message = "Supplier email cannot exceed 150 characters"
    )
    @Column(
            nullable = false,
            unique = true,
            length = 150
    )
    private String email;

    @NotBlank(message = "Supplier phone is required")
    @Pattern(
            regexp = "^[0-9]{10,15}$",
            message = "Supplier phone must contain 10 to 15 digits"
    )
    @Column(
            nullable = false,
            length = 15
    )
    private String phone;

    @NotBlank(message = "Supplier address is required")
    @Size(
            max = 250,
            message = "Supplier address cannot exceed 250 characters"
    )
    @Column(
            nullable = false,
            length = 250
    )
    private String address;

    @NotBlank(message = "Supplier city is required")
    @Size(
            max = 100,
            message = "Supplier city cannot exceed 100 characters"
    )
    @Column(
            nullable = false,
            length = 100
    )
    private String city;

    @NotBlank(message = "Supplier state is required")
    @Size(
            max = 100,
            message = "Supplier state cannot exceed 100 characters"
    )
    @Column(
            nullable = false,
            length = 100
    )
    private String state;

    @NotBlank(message = "Supplier pincode is required")
    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "Supplier pincode must contain exactly 6 digits"
    )
    @Column(
            nullable = false,
            length = 6
    )
    private String pincode;

    @Size(
            max = 50,
            message = "Tax ID cannot exceed 50 characters"
    )
    @Column(length = 50)
    private String taxId;

    @Size(
            max = 100,
            message = "Payment terms cannot exceed 100 characters"
    )
    @Column(length = 100)
    private String paymentTerms;

    @NotNull(message = "Supplier status is required")
    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private SupplierStatus status = SupplierStatus.ACTIVE;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = SupplierStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public SupplierStatus getStatus() {
        return status;
    }

    public void setStatus(SupplierStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}