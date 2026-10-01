package com.hcl.inventory.service;

import com.hcl.inventory.entity.Warehouse;
import com.hcl.inventory.enums.WarehouseStatus;
import com.hcl.inventory.exception.DuplicateWarehouseCodeException;
import com.hcl.inventory.exception.WarehouseNotFoundException;
import com.hcl.inventory.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Autowired
    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    public List<Warehouse> getAllWarehouses() {

        return warehouseRepository.findAll();
    }

    public Warehouse getWarehouseById(Integer warehouseId) {

        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() ->
                        new WarehouseNotFoundException(
                                "Warehouse with id " + warehouseId + " not found"
                        )
                );
    }

    public Warehouse addWarehouse(Warehouse warehouse) {

        if (warehouseRepository.existsByWarehouseCode(
                warehouse.getWarehouseCode())) {

            throw new DuplicateWarehouseCodeException(
                    "Warehouse with code "
                            + warehouse.getWarehouseCode()
                            + " already exists"
            );
        }

        if (warehouse.getStatus() == null) {
            warehouse.setStatus(WarehouseStatus.ACTIVE);
        }

        return warehouseRepository.save(warehouse);
    }

    public Warehouse updateWarehouse(
            Integer warehouseId,
            Warehouse updatedWarehouse
    ) {

        Warehouse existingWarehouse =
                warehouseRepository.findById(warehouseId)
                        .orElseThrow(() ->
                                new WarehouseNotFoundException(
                                        "Warehouse with id "
                                                + warehouseId
                                                + " not found"
                                )
                        );

        if (warehouseRepository
                .existsByWarehouseCodeAndWarehouseIdNot(
                        updatedWarehouse.getWarehouseCode(),
                        warehouseId
                )) {

            throw new DuplicateWarehouseCodeException(
                    "Warehouse with code "
                            + updatedWarehouse.getWarehouseCode()
                            + " already exists"
            );
        }

        existingWarehouse.setWarehouseCode(
                updatedWarehouse.getWarehouseCode()
        );

        existingWarehouse.setWarehouseName(
                updatedWarehouse.getWarehouseName()
        );

        existingWarehouse.setWarehouseLocation(
                updatedWarehouse.getWarehouseLocation()
        );

        existingWarehouse.setAddress(
                updatedWarehouse.getAddress()
        );

        existingWarehouse.setCity(
                updatedWarehouse.getCity()
        );

        existingWarehouse.setState(
                updatedWarehouse.getState()
        );

        existingWarehouse.setPincode(
                updatedWarehouse.getPincode()
        );

        existingWarehouse.setManagerName(
                updatedWarehouse.getManagerName()
        );

        existingWarehouse.setContactNumber(
                updatedWarehouse.getContactNumber()
        );

        existingWarehouse.setEmail(
                updatedWarehouse.getEmail()
        );

        existingWarehouse.setCapacity(
                updatedWarehouse.getCapacity()
        );

        if (updatedWarehouse.getStatus() != null) {
            existingWarehouse.setStatus(
                    updatedWarehouse.getStatus()
            );
        }

        return warehouseRepository.save(existingWarehouse);
    }

    public void deleteWarehouse(Integer warehouseId) {

        Warehouse warehouse = getWarehouseById(warehouseId);

        warehouseRepository.delete(warehouse);
    }

    public List<Warehouse> searchByName(String name) {

        return warehouseRepository
                .findByWarehouseNameContainingIgnoreCase(name);
    }

    public List<Warehouse> searchByCity(String city) {

        return warehouseRepository.findByCityIgnoreCase(city);
    }

    public List<Warehouse> searchByState(String state) {

        return warehouseRepository.findByStateIgnoreCase(state);
    }

    public List<Warehouse> searchByLocation(String location) {

        return warehouseRepository
                .findByWarehouseLocationContainingIgnoreCase(location);
    }
}