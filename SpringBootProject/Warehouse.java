package com.example.warehousemanagement.entity;

import javax.persistence.*;
import java.util.List;

/**
 * Entity representing a Warehouse.
 */
@Entity
@Table(name = "warehouses")
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String timezone;

    @Column(length = 2000)
    private String calendar;

    @ElementCollection
    @CollectionTable(name = "warehouse_policies", joinColumns = @JoinColumn(name = "warehouse_id"))
    @Column(name = "policy")
    private List<String> policies;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getCalendar() { return calendar; }
    public void setCalendar(String calendar) { this.calendar = calendar; }

    public List<String> getPolicies() { return policies; }
    public void setPolicies(List<String> policies) { this.policies = policies; }
}
