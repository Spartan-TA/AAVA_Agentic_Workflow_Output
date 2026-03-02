package com.wems.certification.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CertificationDto {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String status;
    private String notes;
}
