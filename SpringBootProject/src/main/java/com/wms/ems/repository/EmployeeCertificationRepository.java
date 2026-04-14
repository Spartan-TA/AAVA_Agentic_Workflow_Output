package com.wms.ems.repository;

import com.wms.ems.entity.EmployeeCertification;
import com.wms.ems.entity.Employee;
import com.wms.ems.enums.CertificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for EmployeeCertification entity operations.
 * Provides CRUD and custom query methods for employee certification management.
 */
public interface EmployeeCertificationRepository extends JpaRepository<EmployeeCertification, Long> {
    /**
     * Find employee certifications by employee and status.
     * @param employee the employee
     * @param status the certification status
     * @return List of EmployeeCertifications
     */
    List<EmployeeCertification> findByEmployeeAndStatus(Employee employee, CertificationStatus status);

    /**
     * Find employee certifications expiring before a certain date.
     * @param expiryDate the expiry date
     * @return List of EmployeeCertifications
     */
    List<EmployeeCertification> findByExpiryDateBefore(LocalDate expiryDate);
}
