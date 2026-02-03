package com.wms.core.service;

import com.wms.core.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EmployeeService {
    Employee create(Employee employee);
    Employee update(Long id, Employee employee);
    void softDelete(Long id);
    Optional<Employee> findById(Long id);
    Page<Employee> findAll(String filter, Pageable pageable);
}