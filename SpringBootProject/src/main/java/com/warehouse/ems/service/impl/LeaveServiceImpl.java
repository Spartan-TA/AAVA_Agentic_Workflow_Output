package com.warehouse.ems.service.impl;

import com.warehouse.ems.entity.LeaveRequest;
import com.warehouse.ems.repository.LeaveRequestRepository;
import com.warehouse.ems.dto.LeaveRequestDto;
import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveServiceImpl implements LeaveService {
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    // Helper method to map LeaveRequest to LeaveRequestDto
    private LeaveRequestDto mapToDto(LeaveRequest leaveRequest) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(leaveRequest.getId());
        dto.setEmployeeId(leaveRequest.getEmployee().getId());
        dto.setStartDate(leaveRequest.getStartDate());
        dto.setEndDate(leaveRequest.getEndDate());
        dto.setType(leaveRequest.getType());
        dto.setStatus(leaveRequest.getStatus());
        // Add other fields as needed
        return dto;
    }

    @Override
    public LeaveRequestDto getLeaveRequestById(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest not found with id: " + id));
        return mapToDto(leaveRequest);
    }

    @Override
    public List<LeaveRequestDto> getAllLeaveRequests() {
        return leaveRequestRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public LeaveRequestDto createLeaveRequest(LeaveRequestDto leaveRequestDto) {
        LeaveRequest leaveRequest = new LeaveRequest();
        // Assume Employee is set elsewhere or via service
        leaveRequest.setStartDate(leaveRequestDto.getStartDate());
        leaveRequest.setEndDate(leaveRequestDto.getEndDate());
        leaveRequest.setType(leaveRequestDto.getType());
        leaveRequest.setStatus("PENDING");
        // Set other fields as needed
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return mapToDto(saved);
    }

    @Override
    public LeaveRequestDto approveLeaveRequest(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest not found with id: " + id));
        leaveRequest.setStatus("APPROVED");
        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        return mapToDto(updated);
    }

    @Override
    public void deleteLeaveRequest(Long id) {
        if (!leaveRequestRepository.existsById(id)) {
            throw new ResourceNotFoundException("LeaveRequest not found with id: " + id);
        }
        leaveRequestRepository.deleteById(id);
    }
}
