package com.warehouse.employee.domain;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Tenant entity for multi-tenant support.
 */
@Entity
@Table(name = "tenants", indexes = {
        @Index(name = "idx_tenant_tenant_code", columnList = "tenantCode"),
        @Index(name = "idx_tenant_is_active", columnList = "isActive")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(unique = true, nullable = false)
    private String tenantCode;

    @NotBlank
    @Size(max = 150)
    private String tenantName;

    @NotBlank
    @Size(max = 50)
    private String timezone;

    @NotBlank
    @Size(max = 20)
    private String locale;

    @NotNull
    private Boolean isActive;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
