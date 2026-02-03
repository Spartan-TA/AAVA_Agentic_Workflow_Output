package com.company.wms.schedule;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing the warehouse calendar, including blackout dates and holidays.
 */
@Entity
@Table(name = "warehouse_calendars")
public class WarehouseCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "calendar_id")
    private Set<BlackoutDate> blackoutDates = new HashSet<>();

    @Column(name = "description")
    private String description;

    public WarehouseCalendar() {}

    public WarehouseCalendar(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<BlackoutDate> getBlackoutDates() {
        return blackoutDates;
    }

    public void setBlackoutDates(Set<BlackoutDate> blackoutDates) {
        this.blackoutDates = blackoutDates;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
