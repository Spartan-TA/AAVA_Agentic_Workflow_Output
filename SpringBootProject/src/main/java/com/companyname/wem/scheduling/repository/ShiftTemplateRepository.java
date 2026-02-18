package com.companyname.wem.scheduling.repository;

import com.companyname.wem.scheduling.domain.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {
}
