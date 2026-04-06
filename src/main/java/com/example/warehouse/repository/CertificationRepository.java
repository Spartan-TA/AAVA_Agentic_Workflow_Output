package com.example.warehouse.repository;

import com.example.warehouse.entity.Certification;
import com.example.warehouse.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByEmployeeAndExpiryDateBefore(Employee employee, LocalDate expiryDate);
}
