package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

/**
 * BlackoutDate entity for managing non-working days in the warehouse.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "blackout_dates")
public class BlackoutDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    @Size(max = 100)
    private String reason;

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}
