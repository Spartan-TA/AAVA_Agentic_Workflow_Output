package com.warehouse.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Employee entity.
 * Used for API request/response to decouple the API contract from the internal entity structure.
 * 
 * @author Warehouse Development Team
 * @version 1.0.0
 */
public class EmployeeDTO {
    
    private Long id;
    
    @NotBlank(message = "Badge ID is required")
    private String badgeId;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Role is required")
    private String role;
    
    @NotBlank(message = "Department is required")
    private String department;
    
    private String shiftGroup;
    
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
    
    @NotNull(message = "Status is required")
    private String status;
    
    private boolean deleted;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String createdBy;
    
    private String updatedBy;

    // Constructors
    public EmployeeDTO() {}

    public EmployeeDTO(Long id, String badgeId, String name, String role, String department, 
                       String shiftGroup, LocalDate hireDate, String status) {
        this.id = id;
        this.badgeId = badgeId;
        this.name = name;
        this.role = role;
        this.department = department;
        this.shiftGroup = shiftGroup;
        this.hireDate = hireDate;
        this.status = status;
    }

    /**
     * Factory method to create DTO from Employee entity.
     * 
     * @param employee The employee entity
     * @return EmployeeDTO populated with entity data
     */
    public static EmployeeDTO fromEntity(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setBadgeId(employee.getBadgeId());
        dto.setName(employee.getName());
        dto.setRole(employee.getRole());
        dto.setDepartment(employee.getDepartment());
        dto.setShiftGroup(employee.getShiftGroup());
        dto.setHireDate(employee.getHireDate());
        dto.setStatus(employee.getStatus());
        dto.setDeleted(employee.isDeleted());
        dto.setCreatedAt(employee.getCreatedAt());
        dto.setUpdatedAt(employee.getUpdatedAt());
        dto.setCreatedBy(employee.getCreatedBy());
        dto.setUpdatedBy(employee.getUpdatedBy());
        return dto;
    }

    /**
     * Convert DTO to Employee entity.
     * 
     * @return Employee entity populated with DTO data
     */
    public Employee toEntity() {
        Employee employee = new Employee();
        employee.setId(this.id);
        employee.setBadgeId(this.badgeId);
        employee.setName(this.name);
        employee.setRole(this.role);
        employee.setDepartment(this.department);
        employee.setShiftGroup(this.shiftGroup);
        employee.setHireDate(this.hireDate);
        employee.setStatus(this.status);
        employee.setDeleted(this.deleted);
        return employee;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(String badgeId) {
        this.badgeId = badgeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getShiftGroup() {
        return shiftGroup;
    }

    public void setShiftGroup(String shiftGroup) {
        this.shiftGroup = shiftGroup;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}