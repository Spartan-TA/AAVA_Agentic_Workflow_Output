package com.example.warehousemanagement.repository;

import com.example.warehousemanagement.entity.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for ShiftTemplate entity.
 */
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {
    List<ShiftTemplate> findByWarehouseId(Long warehouseId);
    List<ShiftTemplate> findByActiveTrue();
}
