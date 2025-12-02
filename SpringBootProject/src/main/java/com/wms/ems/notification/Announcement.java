package com.wms.ems.notification;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "announcements")
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDate publishDate;

    private LocalDate expiryDate;

    // Getters and setters omitted for brevity
}
