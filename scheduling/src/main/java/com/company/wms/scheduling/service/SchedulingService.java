package com.company.wms.scheduling.service;

import com.company.wms.scheduling.domain.ShiftTemplate;
import com.company.wms.scheduling.domain.ShiftAssignment;
import com.company.wms.common.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing shift templates and assignments.
 */
@Service
public class SchedulingService {
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Create a shift template.
     */
    @Transactional
    public ShiftTemplate createTemplate(ShiftTemplate template) {
        entityManager.persist(template);
        return template;
    }

    /**
     * Get shift template by ID.
     */
    @Transactional(readOnly = true)
    public ShiftTemplate getTemplate(Long id) {
        ShiftTemplate template = entityManager.find(ShiftTemplate.class, id);
        if (template == null) throw new ResourceNotFoundException("ShiftTemplate not found: " + id);
        return template;
    }

    /**
     * List all shift templates.
     */
    @Transactional(readOnly = true)
    public List<ShiftTemplate> listTemplates() {
        return entityManager.createQuery("SELECT t FROM ShiftTemplate t", ShiftTemplate.class).getResultList();
    }

    /**
     * Assign a shift to an employee.
     */
    @Transactional
    public ShiftAssignment assignShift(ShiftAssignment assignment) {
        entityManager.persist(assignment);
        return assignment;
    }

    /**
     * Get shift assignments for employee.
     */
    @Transactional(readOnly = true)
    public List<ShiftAssignment> getAssignmentsForEmployee(Long employeeId) {
        return entityManager.createQuery("SELECT a FROM ShiftAssignment a WHERE a.employeeId = :employeeId ORDER BY a.shiftDate DESC", ShiftAssignment.class)
                .setParameter("employeeId", employeeId)
                .getResultList();
    }

    /**
     * Get shift assignments for employee in date range.
     */
    @Transactional(readOnly = true)
    public List<ShiftAssignment> getAssignmentsForEmployeeInRange(Long employeeId, LocalDate start, LocalDate end) {
        return entityManager.createQuery("SELECT a FROM ShiftAssignment a WHERE a.employeeId = :employeeId AND a.shiftDate BETWEEN :start AND :end ORDER BY a.shiftDate DESC", ShiftAssignment.class)
                .setParameter("employeeId", employeeId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }
}
