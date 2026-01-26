package com.example.warehouse.safety.repository;

import com.example.warehouse.safety.entity.SafetyIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SafetyRepository extends JpaRepository<SafetyIncident, Long> {
    // Find safety incidents by employee
    List<SafetyIncident> findByEmployeeId(Long employeeId);
}
