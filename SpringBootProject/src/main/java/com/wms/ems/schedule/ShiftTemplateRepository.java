package com.wms.ems.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {
}
