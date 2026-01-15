package com.warehouse.training.entity;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "certifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String type;

    @NotNull
    private LocalDate issueDate;

    @NotNull
    private LocalDate expiryDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    @NotNull
    private Long employeeId;

    public enum Status {
        ACTIVE, EXPIRED, REVOKED, PENDING
    }
}
