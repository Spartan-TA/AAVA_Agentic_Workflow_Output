package com.warehouse.ems.repository;

import com.warehouse.ems.entity.AssetAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Long> {
}
