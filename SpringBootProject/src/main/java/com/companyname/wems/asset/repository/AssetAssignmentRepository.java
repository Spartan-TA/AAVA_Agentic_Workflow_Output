package com.companyname.wems.asset.repository;

import com.companyname.wems.asset.model.AssetAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Long> {
    List<AssetAssignment> findByEmployeeId(Long employeeId);
    List<AssetAssignment> findByStatus(String status);
    List<AssetAssignment> findByReturnDateBeforeAndStatus(LocalDate date, String status);
}