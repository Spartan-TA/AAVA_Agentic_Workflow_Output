package com.example.ems.service;

import com.example.ems.entity.Warehouse;
import com.example.ems.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WarehouseService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    public Optional<Warehouse> getWarehouseById(Long id) {
        return warehouseRepository.findById(id);
    }

    public Warehouse createWarehouse(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    public Warehouse updateWarehouse(Long id, Warehouse updatedWarehouse) {
        return warehouseRepository.findById(id)
                .map(existing -> {
                    existing.setName(updatedWarehouse.getName());
                    existing.setLocation(updatedWarehouse.getLocation());
                    existing.setCode(updatedWarehouse.getCode());
                    existing.setStatus(updatedWarehouse.getStatus());
                    return warehouseRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));
    }

    public void deleteWarehouse(Long id) {
        warehouseRepository.deleteById(id);
    }
}
