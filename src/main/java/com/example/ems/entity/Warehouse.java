package com.example.ems.entity;

import jakarta.persistence.*;

@Entity
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private String code;
    private String status; // e.g., ACTIVE, INACTIVE

    public Warehouse() {}

    // Getters and setters omitted for brevity
}
