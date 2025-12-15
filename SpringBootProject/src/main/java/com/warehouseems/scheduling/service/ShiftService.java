package com.warehouseems.scheduling.service;

import org.springframework.stereotype.Service;

/**
 * Stub service for shift scheduling.
 */
@Service
public class ShiftService {
    /**
     * Check if employee is scheduled now. Stub implementation.
     * @param employeeId Employee ID
     * @return always true (stub)
     */
    public boolean isEmployeeScheduledNow(Long employeeId) {
        // TODO: Implement actual shift schedule logic
        return true;
    }
}
