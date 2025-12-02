package com.wms.ems.schedule;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "warehouse_calendar")
public class WarehouseCalendar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String type; // e.g., WORKDAY, HOLIDAY, BLACKOUT

    private String description;

    // Getters and setters omitted for brevity
}
