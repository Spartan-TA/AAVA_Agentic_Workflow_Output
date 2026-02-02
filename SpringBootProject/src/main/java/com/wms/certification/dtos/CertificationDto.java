package com.wms.certification.dtos;

import lombok.*;

/**
 * Data Transfer Object for Certification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificationDto {
    private Long id;
    private String name;
    private String description;
    private boolean active;
}
