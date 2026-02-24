package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

/**
 * ReviewTemplate entity for defining templates for performance reviews.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "review_templates")
public class ReviewTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}
