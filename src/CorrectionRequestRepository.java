package com.company.wms.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for CorrectionRequest entity.
 */
@Repository
public interface CorrectionRequestRepository extends JpaRepository<CorrectionRequest, Long> {
}
