package com.hcl.inventory.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hcl.inventory.enums.UserRole;
import com.hcl.inventory.enums.UserStatus;
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
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @NotBlank(message = "Username is required")
    @Size(
            min = 4,
            max = 50,
            message = "Username must contain 4 to 50 characters"
    )
    @Pattern(
            regexp = "^[A-Za-z0-9._-]+$",
            message = "Username can contain only letters, numbers, dot, underscore and hyphen"
    )
    @Column(
            nullable = false,
            unique = true,
            length = 50
    )
    private String username;

    /*
     * The API accepts the password during user creation/update,
     * but Jackson will never include it in API responses.
     *
     * The service layer stores the BCrypt encoded value.
     */
    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            max = 100,
            message = "Password must contain 8 to 100 characters"
    )
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(
            nullable = false,
            length = 100
    )
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(
            max = 150,
            message = "Full name cannot exceed 150 characters"
    )
    @Column(
            nullable = false,
            length = 150
    )
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(
            max = 150,
            message = "Email cannot exceed 150 characters"
    )
    @Column(
            nullable = false,
            unique = true,
            length = 150
    )
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9]{10,15}$",
            message = "Phone number must contain 10 to 15 digits"
    )
    @Column(
            nullable = false,
            length = 15
    )
    private String phone;

    @NotNull(message = "User role is required")
    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private UserRole role = UserRole.STORE_STAFF;

    @NotNull(message = "User status is required")
    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private UserStatus status = UserStatus.ACTIVE;

    @NotNull(message = "Failed login attempts value is required")
    @Column(
            nullable = false
    )
    private Integer failedLoginAttempts = 0;

    private LocalDateTime lockedUntil;

    private LocalDateTime lastLoginAt;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            nullable = false
    )
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (role == null) {
            role = UserRole.STORE_STAFF;
        }

        if (status == null) {
            status = UserStatus.ACTIVE;
        }

        if (failedLoginAttempts == null) {
            failedLoginAttempts = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();

        if (failedLoginAttempts == null) {
            failedLoginAttempts = 0;
        }
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(
            Integer failedLoginAttempts
    ) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(
            LocalDateTime lockedUntil
    ) {
        this.lockedUntil = lockedUntil;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(
            LocalDateTime lastLoginAt
    ) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}