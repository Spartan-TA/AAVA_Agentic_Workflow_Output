package com.warehouse.employee.management.repository;

import com.warehouse.employee.management.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

/**
 * Repository for Certification entity.
 * Supports CRUD, pagination, sorting, and soft-delete queries.
 */
public interface CertificationRepository extends JpaRepository<Certification, Long>, JpaSpecificationExecutor<Certification> {
    @Query("SELECT c FROM Certification c WHERE c.deletedAt IS NULL")
    List<Certification> findAllActive();

    @Query("SELECT c FROM Certification c WHERE c.deletedAt IS NULL")
    Page<Certification> findAllActive(Pageable pageable);

    @Query("SELECT c FROM Certification c WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Certification> findActiveById(Long id);

    // Custom query example: Find by employeeId and certificationType
    @Query("SELECT c FROM Certification c WHERE c.employee.id = :employeeId AND c.certificationType = :certificationType AND c.deletedAt IS NULL")
    List<Certification> findActiveByEmployeeIdAndCertificationType(Long employeeId, String certificationType);
}
