package com.warehouse.payroll.entity;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_exports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollExport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private LocalDateTime exportDate;

    @NotBlank
    private String format;

    @NotBlank
    private String filePath;

    @NotBlank
    private String status;

    private String reconciliationStatus;
}
