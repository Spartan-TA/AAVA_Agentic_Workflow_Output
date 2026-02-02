package com.wms.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Auditable entity with createdBy and updatedBy fields for audit trail.
 */
@Getter
@Setter
@SuperBuilder
@MappedSuperclass
public abstract class AuditableEntity extends BaseEntity {
    @Column(updatable = false)
    private String createdBy;

    @Column
    private String updatedBy;
}
