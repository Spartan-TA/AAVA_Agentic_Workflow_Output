package com.warehouse.employee.management.schedule.repository;

import com.warehouse.employee.management.schedule.domain.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftRepo extends JpaRepository<Shift, Long> {
    // Custom query methods if needed
}
