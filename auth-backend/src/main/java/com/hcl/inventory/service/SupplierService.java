package com.hcl.inventory.service;

import com.hcl.inventory.entity.Supplier;
import com.hcl.inventory.enums.SupplierStatus;
import com.hcl.inventory.exception.DuplicateSupplierCodeException;
import com.hcl.inventory.exception.DuplicateSupplierEmailException;
import com.hcl.inventory.exception.SupplierNotFoundException;
import com.hcl.inventory.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Autowired
    public SupplierService(
            SupplierRepository supplierRepository
    ) {
        this.supplierRepository = supplierRepository;
    }

    @Transactional(readOnly = true)
    public List<Supplier> getAllSuppliers() {

        return supplierRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Supplier getSupplierById(
            Integer supplierId
    ) {

        return supplierRepository.findById(supplierId)
                .orElseThrow(() ->
                        new SupplierNotFoundException(
                                "Supplier with id "
                                        + supplierId
                                        + " not found"
                        )
                );
    }

    @Transactional
    public Supplier addSupplier(
            Supplier supplier
    ) {

        String supplierCode =
                normalizeCode(
                        supplier.getSupplierCode()
                );

        String email =
                normalizeEmail(
                        supplier.getEmail()
                );

        checkDuplicateCode(supplierCode);

        checkDuplicateEmail(email);

        supplier.setSupplierCode(supplierCode);
        supplier.setEmail(email);

        if (supplier.getStatus() == null) {
            supplier.setStatus(
                    SupplierStatus.ACTIVE
            );
        }

        return supplierRepository.save(
                supplier
        );
    }

    @Transactional
    public Supplier updateSupplier(
            Integer supplierId,
            Supplier updatedSupplier
    ) {

        Supplier existingSupplier =
                getSupplierById(supplierId);

        String supplierCode =
                normalizeCode(
                        updatedSupplier.getSupplierCode()
                );

        String email =
                normalizeEmail(
                        updatedSupplier.getEmail()
                );

        if (supplierRepository
                .existsBySupplierCodeIgnoreCaseAndSupplierIdNot(
                        supplierCode,
                        supplierId
                )) {

            throw new DuplicateSupplierCodeException(
                    "Supplier with code "
                            + supplierCode
                            + " already exists"
            );
        }

        if (supplierRepository
                .existsByEmailIgnoreCaseAndSupplierIdNot(
                        email,
                        supplierId
                )) {

            throw new DuplicateSupplierEmailException(
                    "Supplier with email "
                            + email
                            + " already exists"
            );
        }

        existingSupplier.setSupplierCode(
                supplierCode
        );

        existingSupplier.setSupplierName(
                updatedSupplier.getSupplierName()
        );

        existingSupplier.setContactPerson(
                updatedSupplier.getContactPerson()
        );

        existingSupplier.setEmail(
                email
        );

        existingSupplier.setPhone(
                updatedSupplier.getPhone()
        );

        existingSupplier.setAddress(
                updatedSupplier.getAddress()
        );

        existingSupplier.setCity(
                updatedSupplier.getCity()
        );

        existingSupplier.setState(
                updatedSupplier.getState()
        );

        existingSupplier.setPincode(
                updatedSupplier.getPincode()
        );

        existingSupplier.setTaxId(
                updatedSupplier.getTaxId()
        );

        existingSupplier.setPaymentTerms(
                updatedSupplier.getPaymentTerms()
        );

        if (updatedSupplier.getStatus() != null) {

            existingSupplier.setStatus(
                    updatedSupplier.getStatus()
            );
        }

        return supplierRepository.save(
                existingSupplier
        );
    }

    @Transactional
    public Supplier activateSupplier(
            Integer supplierId
    ) {

        Supplier supplier =
                getSupplierById(supplierId);

        if (supplier.getStatus()
                == SupplierStatus.BLOCKED) {

            throw new IllegalStateException(
                    "A BLOCKED supplier cannot be activated directly"
            );
        }

        supplier.setStatus(
                SupplierStatus.ACTIVE
        );

        return supplierRepository.save(
                supplier
        );
    }

    @Transactional
    public Supplier deactivateSupplier(
            Integer supplierId
    ) {

        Supplier supplier =
                getSupplierById(supplierId);

        supplier.setStatus(
                SupplierStatus.INACTIVE
        );

        return supplierRepository.save(
                supplier
        );
    }

    @Transactional
    public Supplier blockSupplier(
            Integer supplierId
    ) {

        Supplier supplier =
                getSupplierById(supplierId);

        supplier.setStatus(
                SupplierStatus.BLOCKED
        );

        return supplierRepository.save(
                supplier
        );
    }

    @Transactional
    public void deleteSupplier(
            Integer supplierId
    ) {

        Supplier supplier =
                getSupplierById(supplierId);

        supplierRepository.delete(
                supplier
        );
    }

    @Transactional(readOnly = true)
    public Supplier getBySupplierCode(
            String supplierCode
    ) {

        String normalizedCode =
                normalizeCode(supplierCode);

        return supplierRepository
                .findBySupplierCodeIgnoreCase(
                        normalizedCode
                )
                .orElseThrow(() ->
                        new SupplierNotFoundException(
                                "Supplier with code "
                                        + normalizedCode
                                        + " not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Supplier getByEmail(
            String email
    ) {

        String normalizedEmail =
                normalizeEmail(email);

        return supplierRepository
                .findByEmailIgnoreCase(
                        normalizedEmail
                )
                .orElseThrow(() ->
                        new SupplierNotFoundException(
                                "Supplier with email "
                                        + normalizedEmail
                                        + " not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Supplier> searchByName(
            String name
    ) {

        return supplierRepository
                .findBySupplierNameContainingIgnoreCase(
                        name
                );
    }

    @Transactional(readOnly = true)
    public List<Supplier> searchByContactPerson(
            String contactPerson
    ) {

        return supplierRepository
                .findByContactPersonContainingIgnoreCase(
                        contactPerson
                );
    }

    @Transactional(readOnly = true)
    public List<Supplier> searchByCity(
            String city
    ) {

        return supplierRepository
                .findByCityIgnoreCase(city);
    }

    @Transactional(readOnly = true)
    public List<Supplier> searchByState(
            String state
    ) {

        return supplierRepository
                .findByStateIgnoreCase(state);
    }

    @Transactional(readOnly = true)
    public List<Supplier> getByStatus(
            SupplierStatus status
    ) {

        return supplierRepository
                .findByStatus(status);
    }

    private void checkDuplicateCode(
            String supplierCode
    ) {

        if (supplierRepository
                .existsBySupplierCodeIgnoreCase(
                        supplierCode
                )) {

            throw new DuplicateSupplierCodeException(
                    "Supplier with code "
                            + supplierCode
                            + " already exists"
            );
        }
    }

    private void checkDuplicateEmail(
            String email
    ) {

        if (supplierRepository
                .existsByEmailIgnoreCase(email)
        ) {

            throw new DuplicateSupplierEmailException(
                    "Supplier with email "
                            + email
                            + " already exists"
            );
        }
    }

    private String normalizeCode(
            String supplierCode
    ) {

        if (supplierCode == null) {
            return null;
        }

        return supplierCode
                .trim()
                .toUpperCase();
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null) {
            return null;
        }

        return email
                .trim()
                .toLowerCase();
    }
}