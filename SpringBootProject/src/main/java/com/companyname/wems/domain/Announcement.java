package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

/**
 * Announcement entity for posting announcements to employees.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "announcements")
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    private String content;

    @NotNull
    private LocalDateTime postedAt;

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}
