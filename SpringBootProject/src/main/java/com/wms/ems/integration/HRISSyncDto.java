package com.wms.ems.integration;

import lombok.Data;

@Data
public class HRISSyncDto {
    private Long employeeId;
    private String action; // CREATE, UPDATE, TERMINATE
    // Additional fields as needed
}
