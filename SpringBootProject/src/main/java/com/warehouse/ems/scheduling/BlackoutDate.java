package com.warehouse.ems.scheduling;

import com.warehouse.ems.common.BaseEntity;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Entity representing a blackout date when no shifts can be scheduled.
 */
@Entity
@Table(name = "blackout_dates")
public class BlackoutDate extends BaseEntity {

    @NotNull
    @Column(name = "date", nullable = false, unique = true)
    private LocalDate date;

    @Column(name = "reason")
    private String reason;

    public BlackoutDate() {
    }

    public BlackoutDate(LocalDate date, String reason) {
        this.date = date;
        this.reason = reason;
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
