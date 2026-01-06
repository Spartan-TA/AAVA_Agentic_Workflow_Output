package com.example.warehouse.service;

import com.example.warehouse.dto.PerformanceReviewDTO;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.entity.PerformanceReview;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;
import com.example.warehouse.repository.EmployeeRepository;
import com.example.warehouse.repository.PerformanceReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing performance reviews.
 */
@Service
public class PerformanceReviewService {
    private final PerformanceReviewRepository performanceReviewRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public PerformanceReviewService(PerformanceReviewRepository performanceReviewRepository, EmployeeRepository employeeRepository) {
        this.performanceReviewRepository = performanceReviewRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Get all performance reviews for an employee.
     * @param employeeId Employee ID
     * @return List of PerformanceReviewDTO
     */
    @Transactional(readOnly = true)
    public List<PerformanceReviewDTO> getReviewsByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return performanceReviewRepository.findByEmployee(employee).stream()
                .map(PerformanceReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Add a performance review for an employee.
     * @param employeeId Employee ID
     * @param dto PerformanceReviewDTO
     * @return PerformanceReviewDTO
     */
    @Transactional
    public PerformanceReviewDTO addReview(Long employeeId, PerformanceReviewDTO dto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        if (dto.getReviewDate() == null) {
            throw new ValidationException("Review date is required");
        }
        if (dto.getReviewer() == null || dto.getReviewer().isEmpty()) {
            throw new ValidationException("Reviewer is required");
        }
        PerformanceReview review = new PerformanceReview();
        review.setEmployee(employee);
        review.setReviewDate(dto.getReviewDate());
        review.setReviewer(dto.getReviewer());
        review.setComments(dto.getComments());
        review.setScore(dto.getScore());
        performanceReviewRepository.save(review);
        return PerformanceReviewDTO.fromEntity(review);
    }

    /**
     * Get all performance reviews.
     * @return List of PerformanceReviewDTO
     */
    @Transactional(readOnly = true)
    public List<PerformanceReviewDTO> getAllReviews() {
        return performanceReviewRepository.findAll().stream()
                .map(PerformanceReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
