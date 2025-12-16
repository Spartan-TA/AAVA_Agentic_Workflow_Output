package com.companyname.wems.scheduling.repository;

import com.companyname.wems.scheduling.model.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {
    // Custom query methods can be added here
}