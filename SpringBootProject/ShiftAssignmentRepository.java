package com.example.warehousemanagement.repository;

import com.example.warehousemanagement.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for ShiftAssignment entity.
 */
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    List<ShiftAssignment> findByEmployeeId(Long employeeId);
    List<ShiftAssignment> findByShiftDate(LocalDate shiftDate);
    List<ShiftAssignment> findByWarehouseId(Long warehouseId);
}
