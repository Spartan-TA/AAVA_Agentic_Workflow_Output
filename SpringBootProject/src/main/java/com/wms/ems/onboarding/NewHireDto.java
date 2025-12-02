package com.wms.ems.onboarding;

import lombok.Data;

@Data
public class NewHireDto {
    private Long employeeId;
    private String department;
    private String role;
    // Additional fields as needed
}
