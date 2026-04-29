package com.example.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PayrollExport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime exportTime;
    private String status; // e.g., PENDING, SUCCESS, FAILED
    private String provider; // e.g., ADP, Workday
    private String fileName;
    private String deliveryMethod; // e.g., SFTP, API
    private String errorMessage;

    @Lob
    private byte[] exportFile;

    public PayrollExport() {}

    // Getters and setters omitted for brevity
}
