package com.example.warehouse.repository;

import com.example.warehouse.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Certification entity.
 */
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    @Query("SELECT c FROM Certification c WHERE c.expiryDate BETWEEN :from AND :to")
    List<Certification> findExpiringBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT c FROM Certification c WHERE c.employee.id = :employeeId")
    List<Certification> findByEmployee(@Param("employeeId") Long employeeId);
}
