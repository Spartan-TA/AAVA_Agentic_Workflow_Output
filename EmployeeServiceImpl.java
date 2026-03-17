package com.wms.ems.employee.service.impl;

import com.wms.ems.employee.dto.EmployeeDTO;
import com.wms.ems.employee.mapper.EmployeeMapper;
import com.wms.ems.employee.model.Employee;
import com.wms.ems.employee.repository.EmployeeRepository;
import com.wms.ems.employee.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public EmployeeDTO createEmployee(@Valid EmployeeDTO employeeDTO) {
        if (employeeRepository.findByBadgeId(employeeDTO.getBadgeId()).isPresent()) {
            throw new IllegalArgumentException("Badge ID already exists.");
        }
        Employee employee = employeeMapper.toEntity(employeeDTO);
        employee.setDeleted(false);
        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toDto(saved);
    }

    @Override
    public EmployeeDTO updateEmployee(Long id, @Valid EmployeeDTO employeeDTO) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        employee.setName(employeeDTO.getName());
        employee.setBadgeId(employeeDTO.getBadgeId());
        employee.setRole(employeeDTO.getRole());
        employee.setDepartment(employeeDTO.getDepartment());
        employee.setShiftGroup(employeeDTO.getShiftGroup());
        employee.setHireDate(employeeDTO.getHireDate());
        employee.setStatus(employeeDTO.getStatus());
        Employee updated = employeeRepository.save(employee);
        return employeeMapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        return employeeMapper.toDto(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAllActive().stream()
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getEmployeesByDepartment(String department) {
        return employeeRepository.findByDepartment(department).stream()
                .filter(e -> !e.isDeleted())
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getEmployeesByRole(String role) {
        return employeeRepository.findByRole(role).stream()
                .filter(e -> !e.isDeleted())
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getEmployeesByShiftGroup(String shiftGroup) {
        return employeeRepository.findByShiftGroup(shiftGroup).stream()
                .filter(e -> !e.isDeleted())
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getEmployeesByStatus(String status) {
        return employeeRepository.findByStatus(status).stream()
                .filter(e -> !e.isDeleted())
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }
}
