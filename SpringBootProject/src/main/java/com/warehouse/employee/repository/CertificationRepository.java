package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Certification;
import com.warehouse.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Certification entity with custom query methods.
 */
@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {

    /**
     * Find certifications for an employee that expire before a given date.
     * @param employee Employee
     * @param expiryDate Expiry date
     * @return List of Certification
     */
    List<Certification> findByEmployeeAndExpiryDateBefore(Employee employee, LocalDate expiryDate);
}
