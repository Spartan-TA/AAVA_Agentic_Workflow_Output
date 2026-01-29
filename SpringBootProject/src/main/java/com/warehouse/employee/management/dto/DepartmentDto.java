package com.warehouse.employee.management.dto;

import javax.validation.constraints.*;

public class DepartmentDto {
    @NotBlank
    private String name;

    @NotBlank
    private String location;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
