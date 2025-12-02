package com.wms.ems.integration;

import lombok.Data;

@Data
public class WMSSyncDto {
    private Long departmentId;
    private String action; // CREATE, UPDATE
    // Additional fields as needed
}
