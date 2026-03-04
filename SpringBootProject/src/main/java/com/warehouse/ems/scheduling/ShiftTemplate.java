package com.warehouse.ems.scheduling;

import com.warehouse.ems.warehouse.Warehouse;
import com.warehouse.ems.common.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Table(name = "shift_template")
@Schema(description = "Shift template entity for scheduling")
public class ShiftTemplate extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Shift template ID")
    private Long id;

    @NotBlank
    @Schema(description = "Shift name")
    private String name;

    @NotNull
    @Schema(description = "Shift start time")
    private LocalTime startTime;

    @NotNull
    @Schema(description = "Shift end time")
    private LocalTime endTime;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "shift_days", joinColumns = @JoinColumn(name = "shift_id"))
    @Column(name = "day_of_week")
    @Schema(description = "Days of week for the shift")
    private Set<String> daysOfWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    @Schema(description = "Warehouse associated with this shift template")
    private Warehouse warehouse;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Set<String> getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(Set<String> daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
}
