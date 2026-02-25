package com.warehouse.employee.management.domain.employee;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class EmergencyContact {
    private String name;
    private String relationship;
    private String phone;
    private String email;
}
