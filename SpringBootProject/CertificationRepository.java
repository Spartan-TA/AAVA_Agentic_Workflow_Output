package com.example.warehousemanagement.repository;

import com.example.warehousemanagement.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for Certification entity.
 */
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByType(String type);
    List<Certification> findByEmployeeId(Long employeeId);
}
