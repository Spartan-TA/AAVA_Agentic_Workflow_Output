package com.company.wms.leave;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing leave requests and balances.
 */
@Service
public class LeaveService {
    private static final Logger logger = LoggerFactory.getLogger(LeaveService.class);

    private final LeaveRepository leaveRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    public LeaveService(LeaveRepository leaveRepository, LeaveBalanceRepository leaveBalanceRepository) {
        this.leaveRepository = leaveRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
    }

    public List<LeaveRequest> getAllLeaveRequests() {
        logger.info("Fetching all leave requests");
        return leaveRepository.findAll();
    }

    public Optional<LeaveRequest> getLeaveRequestById(Long id) {
        logger.info("Fetching leave request with id {}", id);
        return leaveRepository.findById(id);
    }

    @Transactional
    public LeaveRequest createLeaveRequest(@Valid @NotNull LeaveRequest leaveRequest) {
        logger.info("Creating new leave request for employee {}", leaveRequest.getEmployeeId());
        return leaveRepository.save(leaveRequest);
    }

    @Transactional
    public LeaveRequest updateLeaveRequest(Long id, @Valid @NotNull LeaveRequest leaveRequest) {
        logger.info("Updating leave request with id {}", id);
        leaveRequest.setId(id);
        return leaveRepository.save(leaveRequest);
    }

    @Transactional
    public void deleteLeaveRequest(Long id) {
        logger.info("Deleting leave request with id {}", id);
        leaveRepository.deleteById(id);
    }

    public List<LeaveBalance> getLeaveBalancesByEmployee(Long employeeId) {
        logger.info("Fetching leave balances for employee {}", employeeId);
        return leaveBalanceRepository.findByEmployeeId(employeeId);
    }

    @Transactional
    public LeaveBalance updateLeaveBalance(@Valid @NotNull LeaveBalance leaveBalance) {
        logger.info("Updating leave balance for employee {} and type {}", leaveBalance.getEmployeeId(), leaveBalance.getLeaveType());
        return leaveBalanceRepository.save(leaveBalance);
    }
}
