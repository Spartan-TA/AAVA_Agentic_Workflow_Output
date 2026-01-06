package com.example.warehouse.service;

import com.example.warehouse.dto.EmployeeDTO;
import com.example.warehouse.dto.AttendanceEventDTO;
import com.example.warehouse.dto.PerformanceReviewDTO;
import com.example.warehouse.repository.EmployeeRepository;
import com.example.warehouse.repository.AttendanceEventRepository;
import com.example.warehouse.repository.PerformanceReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for generating reports.
 */
@Service
public class ReportingService {
    private final EmployeeRepository employeeRepository;
    private final AttendanceEventRepository attendanceEventRepository;
    private final PerformanceReviewRepository performanceReviewRepository;

    @Autowired
    public ReportingService(EmployeeRepository employeeRepository, AttendanceEventRepository attendanceEventRepository, PerformanceReviewRepository performanceReviewRepository) {
        this.employeeRepository = employeeRepository;
        this.attendanceEventRepository = attendanceEventRepository;
        this.performanceReviewRepository = performanceReviewRepository;
    }

    /**
     * Get all employees for reporting.
     * @return List of EmployeeDTO
     */
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getEmployeeReport() {
        return employeeRepository.findAll().stream()
                .map(EmployeeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all attendance events for reporting.
     * @return List of AttendanceEventDTO
     */
    @Transactional(readOnly = true)
    public List<AttendanceEventDTO> getAttendanceReport() {
        return attendanceEventRepository.findAll().stream()
                .map(AttendanceEventDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all performance reviews for reporting.
     * @return List of PerformanceReviewDTO
     */
    @Transactional(readOnly = true)
    public List<PerformanceReviewDTO> getPerformanceReviewReport() {
        return performanceReviewRepository.findAll().stream()
                .map(PerformanceReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
