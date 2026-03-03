package com.wms.ems.scheduling.model;

import com.wms.ems.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalTime;

/**
 * Entity representing a shift template in the scheduling module.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shift_templates")
public class ShiftTemplate extends BaseEntity {

    /**
     * Name of the shift template.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Start time of the shift.
     */
    private LocalTime startTime;

    /**
     * End time of the shift.
     */
    private LocalTime endTime;

    /**
     * Whether the shift is recurring.
     */
    @Builder.Default
    private boolean recurring = false;

    /**
     * Whether the shift is eligible for overtime.
     */
    @Builder.Default
    private boolean overtimeEligible = false;
}
