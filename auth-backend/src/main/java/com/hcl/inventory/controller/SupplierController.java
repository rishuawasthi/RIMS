package com.hcl.inventory.controller;

import com.hcl.inventory.entity.Supplier;
import com.hcl.inventory.enums.SupplierStatus;
import com.hcl.inventory.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    @Autowired
    public SupplierController(
            SupplierService supplierService
    ) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public ResponseEntity<List<Supplier>>
    getAllSuppliers() {

        return ResponseEntity.ok(
                supplierService.getAllSuppliers()
        );
    }

    @GetMapping("/{supplierId}")
    public ResponseEntity<Supplier>
    getSupplierById(
            @PathVariable Integer supplierId
    ) {

        return ResponseEntity.ok(
                supplierService.getSupplierById(
                        supplierId
                )
        );
    }

    @PostMapping
    public ResponseEntity<Supplier>
    addSupplier(
            @Valid @RequestBody Supplier supplier
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        supplierService.addSupplier(
                                supplier
                        )
                );
    }

    @PutMapping("/{supplierId}")
    public ResponseEntity<Supplier>
    updateSupplier(
            @PathVariable Integer supplierId,
            @Valid @RequestBody Supplier supplier
    ) {

        return ResponseEntity.ok(
                supplierService.updateSupplier(
                        supplierId,
                        supplier
                )
        );
    }

    @DeleteMapping("/{supplierId}")
    public ResponseEntity<Void>
    deleteSupplier(
            @PathVariable Integer supplierId
    ) {

        supplierService.deleteSupplier(
                supplierId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/code/{supplierCode}")
    public ResponseEntity<Supplier>
    getBySupplierCode(
            @PathVariable String supplierCode
    ) {

        return ResponseEntity.ok(
                supplierService.getBySupplierCode(
                        supplierCode
                )
        );
    }

    @GetMapping("/email")
    public ResponseEntity<Supplier>
    getByEmail(
            @RequestParam String email
    ) {

        return ResponseEntity.ok(
                supplierService.getByEmail(email)
        );
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<Supplier>>
    searchByName(
            @RequestParam String name
    ) {

        return ResponseEntity.ok(
                supplierService.searchByName(name)
        );
    }

    @GetMapping("/search/contact-person")
    public ResponseEntity<List<Supplier>>
    searchByContactPerson(
            @RequestParam String contactPerson
    ) {

        return ResponseEntity.ok(
                supplierService.searchByContactPerson(
                        contactPerson
                )
        );
    }

    @GetMapping("/search/city")
    public ResponseEntity<List<Supplier>>
    searchByCity(
            @RequestParam String city
    ) {

        return ResponseEntity.ok(
                supplierService.searchByCity(city)
        );
    }

    @GetMapping("/search/state")
    public ResponseEntity<List<Supplier>>
    searchByState(
            @RequestParam String state
    ) {

        return ResponseEntity.ok(
                supplierService.searchByState(state)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Supplier>>
    getByStatus(
            @PathVariable SupplierStatus status
    ) {

        return ResponseEntity.ok(
                supplierService.getByStatus(status)
        );
    }

    @PostMapping("/{supplierId}/activate")
    public ResponseEntity<Supplier>
    activateSupplier(
            @PathVariable Integer supplierId
    ) {

        return ResponseEntity.ok(
                supplierService.activateSupplier(
                        supplierId
                )
        );
    }

    @PostMapping("/{supplierId}/deactivate")
    public ResponseEntity<Supplier>
    deactivateSupplier(
            @PathVariable Integer supplierId
    ) {

        return ResponseEntity.ok(
                supplierService.deactivateSupplier(
                        supplierId
                )
        );
    }

    @PostMapping("/{supplierId}/block")
    public ResponseEntity<Supplier>
    blockSupplier(
            @PathVariable Integer supplierId
    ) {

        return ResponseEntity.ok(
                supplierService.blockSupplier(
                        supplierId
                )
        );
    }
}