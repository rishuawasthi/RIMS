package com.hcl.inventory.repository;

import com.hcl.inventory.entity.Supplier;
import com.hcl.inventory.enums.SupplierStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository
        extends JpaRepository<Supplier, Integer> {

    boolean existsBySupplierCodeIgnoreCase(
            String supplierCode
    );

    boolean existsBySupplierCodeIgnoreCaseAndSupplierIdNot(
            String supplierCode,
            Integer supplierId
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    boolean existsByEmailIgnoreCaseAndSupplierIdNot(
            String email,
            Integer supplierId
    );

    Optional<Supplier> findBySupplierCodeIgnoreCase(
            String supplierCode
    );

    Optional<Supplier> findByEmailIgnoreCase(
            String email
    );

    List<Supplier> findBySupplierNameContainingIgnoreCase(
            String supplierName
    );

    List<Supplier> findByContactPersonContainingIgnoreCase(
            String contactPerson
    );

    List<Supplier> findByCityIgnoreCase(
            String city
    );

    List<Supplier> findByStateIgnoreCase(
            String state
    );

    List<Supplier> findByStatus(
            SupplierStatus status
    );
}