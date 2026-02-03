package com.company.wms.schedule;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Entity representing a blackout date when no shifts can be scheduled.
 */
@Entity
@Table(name = "blackout_dates")
public class BlackoutDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "date", unique = true)
    private LocalDate date;

    @Column(name = "reason")
    private String reason;

    public BlackoutDate() {}

    public BlackoutDate(LocalDate date, String reason) {
        this.date = date;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
