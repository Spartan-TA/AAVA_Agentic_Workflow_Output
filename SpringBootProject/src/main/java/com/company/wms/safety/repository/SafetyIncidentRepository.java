package com.company.wms.safety.repository;

import com.company.wms.safety.model.SafetyIncident;
import com.company.wms.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for SafetyIncident entity.
 */
@Repository
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    List<SafetyIncident> findByReportedBy(Employee reportedBy);
}
