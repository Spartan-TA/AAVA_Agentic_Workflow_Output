package com.warehouse.employee.repository;

import com.warehouse.employee.entity.SelfServicePortal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SelfServicePortalRepository extends JpaRepository<SelfServicePortal, Long> {
}
