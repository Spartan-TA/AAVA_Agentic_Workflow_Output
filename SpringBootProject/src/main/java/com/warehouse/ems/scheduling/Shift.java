package com.warehouse.ems.scheduling;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "shift")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_recurring")
    private Boolean isRecurring = false;

    @Column(name = "rotation_group")
    private String rotationGroup;

    @Column(name = "blackout_date")
    private java.sql.Date blackoutDate;

    @Column(name = "warehouse_id")
    private Long warehouseId;
}
