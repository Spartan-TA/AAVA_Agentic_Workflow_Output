package com.companyname.wems.employee;

import com.companyname.wems.dto.EmployeeRequestDto;
import com.companyname.wems.dto.EmployeeResponseDto;
import com.companyname.wems.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDto>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Employee> employees = employeeService.getAllActiveEmployees(pageable);
        Page<EmployeeResponseDto> dtoPage = employees.map(EmployeeResponseDto::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(EmployeeResponseDto.fromEntity(employee));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PostMapping
    public ResponseEntity<EmployeeResponseDto> createEmployee(@Valid @RequestBody EmployeeRequestDto requestDto) {
        Employee employee = employeeService.createEmployee(requestDto.toEntity());
        return new ResponseEntity<>(EmployeeResponseDto.fromEntity(employee), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDto requestDto) {
        Employee updated = employeeService.updateEmployee(id, requestDto.toEntity());
        return ResponseEntity.ok(EmployeeResponseDto.fromEntity(updated));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
