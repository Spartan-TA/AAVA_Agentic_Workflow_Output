package com.example.warehouse.repository;

import com.example.warehouse.entity.Asset;
import com.example.warehouse.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByAssignedEmployee(Employee employee);
}
