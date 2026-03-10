package com.example.warehouse.leave;

import javax.persistence.*;

@Entity
public class LeaveBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long employeeId;
    private int annualLeave;
    private int sickLeave;
    private int casualLeave;

    public LeaveBalance() {}

    public LeaveBalance(Long employeeId, int annualLeave, int sickLeave, int casualLeave) {
        this.employeeId = employeeId;
        this.annualLeave = annualLeave;
        this.sickLeave = sickLeave;
        this.casualLeave = casualLeave;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public int getAnnualLeave() {
        return annualLeave;
    }

    public void setAnnualLeave(int annualLeave) {
        this.annualLeave = annualLeave;
    }

    public int getSickLeave() {
        return sickLeave;
    }

    public void setSickLeave(int sickLeave) {
        this.sickLeave = sickLeave;
    }

    public int getCasualLeave() {
        return casualLeave;
    }

    public void setCasualLeave(int casualLeave) {
        this.casualLeave = casualLeave;
    }
}
