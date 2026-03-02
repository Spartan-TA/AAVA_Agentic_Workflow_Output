package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Certification;
import com.warehouse.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Certification entity.
 */
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    /**
     * Find certifications for an employee by status.
     */
    List<Certification> findByEmployeeAndStatus(Employee employee, Certification.Status status);

    /**
     * Find certifications expiring before a given date.
     */
    List<Certification> findByExpiryDateBefore(LocalDate date);

    /**
     * Find certifications expiring within a certain period.
     */
    @Query("SELECT c FROM Certification c WHERE c.expiryDate BETWEEN :start AND :end")
    List<Certification> findExpiringCertifications(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
