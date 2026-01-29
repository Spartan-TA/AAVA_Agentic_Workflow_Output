package com.warehouse.employee.management.dto;

import javax.validation.constraints.*;
import java.util.List;

public class RoleDto {
    @NotBlank
    private String name;

    @NotNull
    @Size(min = 1)
    private List<String> permissions;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
