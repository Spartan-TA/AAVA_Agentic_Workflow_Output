package com.warehouse.ems.leave;

import com.warehouse.ems.common.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing leave requests.
 */
@Service
public class LeaveService {

    private final LeaveRepository leaveRepository;

    @Autowired
    public LeaveService(LeaveRepository leaveRepository) {
        this.leaveRepository = leaveRepository;
    }

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRepository.findAll();
    }

    public LeaveRequest getLeaveRequestById(Long id) {
        return leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest not found with id: " + id));
    }

    public List<LeaveRequest> getLeaveRequestsByEmployee(Long employeeId) {
        return leaveRepository.findByEmployeeId(employeeId);
    }

    public List<LeaveRequest> getLeaveRequestsByStatus(String status) {
        return leaveRepository.findByStatus(status);
    }

    public List<LeaveRequest> getOverlappingLeaves(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return leaveRepository.findOverlappingLeaves(employeeId, startDate, endDate);
    }

    @Transactional
    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest) {
        // Validate overlapping leaves
        List<LeaveRequest> overlaps = leaveRepository.findOverlappingLeaves(
                leaveRequest.getEmployeeId(), leaveRequest.getStartDate(), leaveRequest.getEndDate()
        );
        if (!overlaps.isEmpty()) {
            throw new IllegalArgumentException("Leave request overlaps with existing leave.");
        }
        leaveRequest.setStatus("PENDING");
        return leaveRepository.save(leaveRequest);
    }

    @Transactional
    public LeaveRequest updateLeaveRequest(Long id, LeaveRequest updated) {
        LeaveRequest existing = getLeaveRequestById(id);
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setLeaveType(updated.getLeaveType());
        existing.setReason(updated.getReason());
        existing.setStatus(updated.getStatus());
        return leaveRepository.save(existing);
    }

    @Transactional
    public void deleteLeaveRequest(Long id) {
        if (!leaveRepository.existsById(id)) {
            throw new ResourceNotFoundException("LeaveRequest not found with id: " + id);
        }
        leaveRepository.deleteById(id);
    }
}
