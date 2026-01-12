// src/main/java/com/company/warehouse/audit/AuditLogRepository.java
package com.company.warehouse.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}