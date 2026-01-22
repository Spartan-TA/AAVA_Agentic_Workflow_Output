package com.warehouse.ems.certification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Certification entity with custom queries for expiry alerts.
 */
@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {

    /**
     * Find certifications expiring within a given number of days.
     * @param date the cutoff date
     * @return list of expiring certifications
     */
    @Query("SELECT c FROM Certification c WHERE c.expiryDate <= :date AND c.status = 'ACTIVE'")
    List<Certification> findExpiringCertifications(@Param("date") LocalDate date);

    /**
     * Find certifications that are already expired.
     * @param date the current date
     * @return list of expired certifications
     */
    @Query("SELECT c FROM Certification c WHERE c.expiryDate < :date AND c.status = 'ACTIVE'")
    List<Certification> findExpiredCertifications(@Param("date") LocalDate date);
}
