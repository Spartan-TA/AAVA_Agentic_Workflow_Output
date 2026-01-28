package com.warehouse.management.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * Base entity with audit fields and soft delete.
 */
@MappedSuperclass
@Getter
@Setter
@SQLDelete(sql = "UPDATE {h-schema}{h-table} SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Version
    private Integer version;

    @Column(name = "deleted")
    private Boolean deleted = false;
}