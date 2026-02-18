package com.companyname.wem.leave.service;

import com.companyname.wem.leave.domain.LeaveRequest;
import com.companyname.wem.leave.domain.LeaveStatus;
import com.companyname.wem.leave.dto.LeaveRequestDTO;
import com.companyname.wem.leave.mapper.LeaveRequestMapper;
import com.companyname.wem.leave.repository.LeaveRequestRepository;
import com.companyname.wem.employee.domain.Employee;
import com.companyname.wem.employee.repository.EmployeeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeaveService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestMapper leaveRequestMapper;

    public List<LeaveRequestDTO> getAllLeaveRequests() {
        return leaveRequestRepository.findAll()
                .stream()
                .map(leaveRequestMapper::toDto)
                .toList();
    }

    public Optional<LeaveRequestDTO> getLeaveRequestById(Long id) {
        return leaveRequestRepository.findById(id)
                .map(leaveRequestMapper::toDto);
    }

    @Transactional
    public LeaveRequestDTO createLeaveRequest(@Valid LeaveRequestDTO dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId()).orElseThrow();
        LeaveRequest entity = leaveRequestMapper.toEntity(dto);
        entity.setEmployee(employee);
        LeaveRequest saved = leaveRequestRepository.save(entity);
        return leaveRequestMapper.toDto(saved);
    }

    @Transactional
    public Optional<LeaveRequestDTO> updateLeaveRequest(Long id, @Valid LeaveRequestDTO dto) {
        return leaveRequestRepository.findById(id)
                .map(existing -> {
                    Employee employee = employeeRepository.findById(dto.getEmployeeId()).orElseThrow();
                    LeaveRequest updated = leaveRequestMapper.toEntity(dto);
                    updated.setId(existing.getId());
                    updated.setEmployee(employee);
                    LeaveRequest saved = leaveRequestRepository.save(updated);
                    return leaveRequestMapper.toDto(saved);
                });
    }

    @Transactional
    public boolean deleteLeaveRequest(Long id) {
        return leaveRequestRepository.findById(id)
                .map(request -> {
                    leaveRequestRepository.delete(request);
                    return true;
                }).orElse(false);
    }

    public List<LeaveRequestDTO> getLeaveRequestsByStatus(LeaveStatus status) {
        return leaveRequestRepository.findByStatus(status)
                .stream()
                .map(leaveRequestMapper::toDto)
                .toList();
    }

    public List<LeaveRequestDTO> getLeaveRequestsByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow();
        return leaveRequestRepository.findByEmployee(employee)
                .stream()
                .map(leaveRequestMapper::toDto)
                .toList();
    }

    public List<LeaveRequestDTO> getLeaveRequestsByDateRange(LocalDate start, LocalDate end) {
        return leaveRequestRepository.findByStartDateBetween(start, end)
                .stream()
                .map(leaveRequestMapper::toDto)
                .toList();
    }
}
