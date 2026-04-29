package com.example.ems.repository;

import com.example.ems.entity.PayrollExport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollExportRepository extends JpaRepository<PayrollExport, Long> {
    // Custom query methods if needed
}
