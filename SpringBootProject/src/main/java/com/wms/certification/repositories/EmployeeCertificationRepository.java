package com.wms.certification.repositories;

import com.wms.certification.model.EmployeeCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing EmployeeCertification entities
 */
@Repository
public interface EmployeeCertificationRepository extends JpaRepository<EmployeeCertification, Long> {
    /**
     * Find all certifications for an employee
     * @param employeeId Employee ID
     * @return List of EmployeeCertification
     */
    List<EmployeeCertification> findByEmployeeId(Long employeeId);
}
