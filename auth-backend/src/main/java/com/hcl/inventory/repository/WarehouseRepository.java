package com.hcl.inventory.repository;

import com.hcl.inventory.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {

    boolean existsByWarehouseCode(String warehouseCode);

    boolean existsByWarehouseCodeAndWarehouseIdNot(
            String warehouseCode,
            Integer warehouseId
    );

    List<Warehouse> findByWarehouseNameContainingIgnoreCase(
            String warehouseName
    );

    List<Warehouse> findByCityIgnoreCase(String city);

    List<Warehouse> findByStateIgnoreCase(String state);

    List<Warehouse> findByWarehouseLocationContainingIgnoreCase(
            String location
    );
}