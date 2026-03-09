package com.company.warehousemgmt.controller;

import com.company.warehousemgmt.domain.Employee;
import com.company.warehousemgmt.domain.Role;
import com.company.warehousemgmt.dto.EmployeeRequestDTO;
import com.company.warehousemgmt.dto.EmployeeResponseDTO;
import com.company.warehousemgmt.repository.RoleRepository;
import com.company.warehousemgmt.service.EmployeeService;
import com.company.warehousemgmt.util.EmployeeMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Employee CRUD operations.
 */
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;
    private final RoleRepository roleRepository;

    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDTO>> getAllEmployees(@ParameterObject Pageable pageable) {
        Page<Employee> page = employeeService.getAllEmployees(pageable);
        List<EmployeeResponseDTO> dtos = page.getContent().stream()
                .map(EmployeeMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new PageImpl<>(dtos, pageable, page.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        return ResponseEntity.ok(EmployeeMapper.toResponseDTO(employee));
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO dto) {
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        Employee employee = EmployeeMapper.toEntity(dto, role);
        Employee saved = employeeService.createEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(EmployeeMapper.toResponseDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDTO dto) {
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        Employee updated = EmployeeMapper.toEntity(dto, role);
        updated.setId(id);
        Employee saved = employeeService.updateEmployee(id, updated);
        return ResponseEntity.ok(EmployeeMapper.toResponseDTO(saved));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> patchEmployee(@PathVariable Long id, @RequestBody EmployeeRequestDTO dto) {
        Employee existing = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        if (dto.getFirstName() != null) existing.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) existing.setLastName(dto.getLastName());
        if (dto.getEmail() != null) existing.setEmail(dto.getEmail());
        if (dto.getDepartment() != null) existing.setDepartment(dto.getDepartment());
        if (dto.getShiftGroup() != null) existing.setShiftGroup(dto.getShiftGroup());
        if (dto.getHireDate() != null) existing.setHireDate(dto.getHireDate());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        if (dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new EntityNotFoundException("Role not found"));
            existing.setRole(role);
        }
        Employee saved = employeeService.updateEmployee(id, existing);
        return ResponseEntity.ok(EmployeeMapper.toResponseDTO(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
