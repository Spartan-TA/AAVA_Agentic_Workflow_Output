package com.wms.ems.payroll;

import lombok.Data;

@Data
public class PayrollExportRequestDto {
    private String provider;
    private String period;
    // Additional fields as needed
}
