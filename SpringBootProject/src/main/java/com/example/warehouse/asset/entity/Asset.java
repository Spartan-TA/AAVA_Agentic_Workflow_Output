package com.example.warehouse.asset.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    @Column(nullable = false)
    private String status; // e.g., ACTIVE, MAINTENANCE, RETIRED

    @Column
    private String location;

    // Constructors
    public Asset() {}

    public Asset(String name, String type, LocalDate purchaseDate, String status, String location) {
        this.name = name;
        this.type = type;
        this.purchaseDate = purchaseDate;
        this.status = status;
        this.location = location;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
