package com.example.warehouse.service;

import com.example.warehouse.entity.Department;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;
import com.example.warehouse.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing departments.
 */
@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    @Autowired
    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    /**
     * Get all departments.
     * @return List of Department
     */
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    /**
     * Get department by ID.
     * @param id Department ID
     * @return Department
     */
    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    /**
     * Create a new department.
     * @param name Department name
     * @return Department
     */
    @Transactional
    public Department createDepartment(String name) {
        if (name == null || name.isEmpty()) {
            throw new ValidationException("Department name is required");
        }
        if (departmentRepository.existsByName(name)) {
            throw new ValidationException("Department name already exists");
        }
        Department department = new Department();
        department.setName(name);
        return departmentRepository.save(department);
    }

    /**
     * Delete a department by ID.
     * @param id Department ID
     */
    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }
        departmentRepository.deleteById(id);
    }
}
