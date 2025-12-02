package com.wms.ems.reporting;

import lombok.Data;

@Data
public class ReportFilterDto {
    private String type;
    private String startDate;
    private String endDate;
    private String department;
    // Additional fields as needed
}
