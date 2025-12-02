package com.wms.ems.asset;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String assetId;

    @Column(nullable = false)
    private String type;

    private String condition;
    private Long assignedEmployeeId;

    @ElementCollection
    private List<CheckoutEvent> checkoutHistory = new ArrayList<>();

    @Embeddable
    public static class CheckoutEvent {
        private Long employeeId;
        private String action; // CHECKOUT or RETURN
        private java.time.LocalDateTime timestamp;
    }

    // Getters and setters omitted for brevity
}
