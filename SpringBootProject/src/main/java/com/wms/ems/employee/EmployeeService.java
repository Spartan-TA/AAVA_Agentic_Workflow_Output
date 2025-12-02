package com.wms.ems.employee;

import com.wms.ems.department.Department;
import com.wms.ems.department.DepartmentRepository;
import com.wms.ems.employee.dto.EmployeeRequestDTO;
import com.wms.ems.employee.dto.EmployeeResponseDTO;
import com.wms.ems.role.Role;
import com.wms.ems.role.RoleRepository;
import com.wms.ems.shift.ShiftGroup;
import com.wms.ems.shift.ShiftGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/**
 * Service for Employee business logic.
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private ShiftGroupRepository shiftGroupRepository;

    public Page<EmployeeResponseDTO> getAllEmployees(String name, Long departmentId, Pageable pageable) {
        return employeeRepository.filter(name, departmentId, pageable)
                .map(this::toResponseDTO);
    }

    public Optional<EmployeeResponseDTO> getEmployee(Long id) {
        return employeeRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .map(this::toResponseDTO);
    }

    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {
        Employee employee = new Employee();
        employee.setName(dto.name);
        employee.setBadgeId(dto.badgeId);
        employee.setRole(roleRepository.findById(dto.roleId).orElse(null));
        employee.setDepartment(departmentRepository.findById(dto.departmentId).orElse(null));
        employee.setShiftGroup(shiftGroupRepository.findById(dto.shiftGroupId).orElse(null));
        employee.setHireDate(dto.hireDate);
        employee.setStatus(dto.status);
        employee.setDeleted(false);
        return toResponseDTO(employeeRepository.save(employee));
    }

    @Transactional
    public Optional<EmployeeResponseDTO> updateEmployee(Long id, EmployeeRequestDTO dto) {
        return employeeRepository.findById(id).filter(e -> !e.getDeleted()).map(employee -> {
            employee.setName(dto.name);
            employee.setBadgeId(dto.badgeId);
            employee.setRole(roleRepository.findById(dto.roleId).orElse(null));
            employee.setDepartment(departmentRepository.findById(dto.departmentId).orElse(null));
            employee.setShiftGroup(shiftGroupRepository.findById(dto.shiftGroupId).orElse(null));
            employee.setHireDate(dto.hireDate);
            employee.setStatus(dto.status);
            return toResponseDTO(employeeRepository.save(employee));
        });
    }

    @Transactional
    public boolean softDeleteEmployee(Long id) {
        return employeeRepository.findById(id).filter(e -> !e.getDeleted()).map(employee -> {
            employee.setDeleted(true);
            employeeRepository.save(employee);
            return true;
        }).orElse(false);
    }

    private EmployeeResponseDTO toResponseDTO(Employee employee) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.id = employee.getId();
        dto.name = employee.getName();
        dto.badgeId = employee.getBadgeId();
        dto.roleName = employee.getRole() != null ? employee.getRole().getName() : null;
        dto.departmentName = employee.getDepartment() != null ? employee.getDepartment().getName() : null;
        dto.shiftGroupName = employee.getShiftGroup() != null ? employee.getShiftGroup().getName() : null;
        dto.hireDate = employee.getHireDate();
        dto.status = employee.getStatus();
        dto.deleted = employee.getDeleted();
        return dto;
    }
}
