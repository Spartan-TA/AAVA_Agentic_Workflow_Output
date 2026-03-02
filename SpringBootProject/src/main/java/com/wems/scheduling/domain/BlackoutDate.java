package com.wems.scheduling.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "blackout_dates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlackoutDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    private String reason;
    private String type;
    private String department;
}
