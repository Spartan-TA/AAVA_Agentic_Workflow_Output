package com.wms.scheduling.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entity representing a blackout date for scheduling.
 */
@Entity
@Table(name = "blackout_date")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlackoutDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    private String reason;
}
