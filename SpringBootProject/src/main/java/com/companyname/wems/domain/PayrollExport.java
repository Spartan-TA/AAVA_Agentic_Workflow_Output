package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

/**
 * PayrollExport entity for exporting payroll data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payroll_exports")
public class PayrollExport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime exportedAt;

    @NotBlank
    @Size(max = 255)
    private String fileUrl;

    @NotBlank
    @Size(max = 20)
    private String status; // PENDING, COMPLETED, FAILED

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}
