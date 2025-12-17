package com.warehouse.employee.management.service;

import com.warehouse.employee.management.entity.LeaveBalance;
import com.warehouse.employee.management.repository.LeaveBalanceRepository;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service class for managing LeaveBalance entities.
 */
@Service
public class LeaveBalanceService {
    private final LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    public LeaveBalanceService(LeaveBalanceRepository leaveBalanceRepository) {
        this.leaveBalanceRepository = leaveBalanceRepository;
    }

    /**
     * Get all leave balances.
     * @return List of leave balances
     */
    public List<LeaveBalance> getAllLeaveBalances() {
        return leaveBalanceRepository.findAll();
    }

    /**
     * Get leave balance by ID.
     * @param id LeaveBalance ID
     * @return LeaveBalance entity
     */
    public LeaveBalance getLeaveBalanceById(Long id) {
        return leaveBalanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveBalance not found with id: " + id));
    }

    /**
     * Create a new leave balance.
     * @param leaveBalance LeaveBalance entity
     * @return Created leave balance
     */
    @Transactional
    public LeaveBalance createLeaveBalance(LeaveBalance leaveBalance) {
        return leaveBalanceRepository.save(leaveBalance);
    }

    /**
     * Update an existing leave balance.
     * @param id LeaveBalance ID
     * @param updatedBalance Updated leave balance entity
     * @return Updated leave balance
     */
    @Transactional
    public LeaveBalance updateLeaveBalance(Long id, LeaveBalance updatedBalance) {
        LeaveBalance existingBalance = getLeaveBalanceById(id);
        existingBalance.setEmployee(updatedBalance.getEmployee());
        existingBalance.setLeaveType(updatedBalance.getLeaveType());
        existingBalance.setBalance(updatedBalance.getBalance());
        // Add other fields as needed
        return leaveBalanceRepository.save(existingBalance);
    }

    /**
     * Delete a leave balance by ID.
     * @param id LeaveBalance ID
     */
    @Transactional
    public void deleteLeaveBalance(Long id) {
        LeaveBalance balance = getLeaveBalanceById(id);
        leaveBalanceRepository.delete(balance);
    }
}
