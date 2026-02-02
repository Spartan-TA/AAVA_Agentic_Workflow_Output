package com.wms.employee.domain;

import com.wms.common.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

/**
 * Department entity representing a department in the warehouse.
 */
@Entity
@Table(name = "departments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department extends BaseEntity {
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private Set<Employee> employees;
}
