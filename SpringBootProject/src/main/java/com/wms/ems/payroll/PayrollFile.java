package com.wms.ems.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PayrollFile {
    private String fileName;
    private String content;
}
