package com.warehouse.equipment.entity;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;

@Entity
@Table(name = "assets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String type;

    @NotBlank
    private String serialNumber;

    @NotBlank
    private String condition;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        AVAILABLE, ASSIGNED, MAINTENANCE, RETIRED
    }
}
