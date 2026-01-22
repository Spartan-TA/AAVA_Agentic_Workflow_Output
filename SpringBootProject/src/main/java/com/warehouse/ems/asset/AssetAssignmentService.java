package com.warehouse.ems.asset;

import com.warehouse.ems.certification.CertificationService;
import com.warehouse.ems.employee.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing asset assignments, check-in/out, certification validation, and overdue tracking.
 */
@Service
public class AssetAssignmentService {
    private final AssetAssignmentRepository assignmentRepository;
    private final EquipmentRepository equipmentRepository;
    private final CertificationService certificationService;

    @Autowired
    public AssetAssignmentService(AssetAssignmentRepository assignmentRepository,
                                  EquipmentRepository equipmentRepository,
                                  CertificationService certificationService) {
        this.assignmentRepository = assignmentRepository;
        this.equipmentRepository = equipmentRepository;
        this.certificationService = certificationService;
    }

    /**
     * Check out equipment to an employee, validating certification.
     */
    @Transactional
    public AssetAssignment checkout(Long equipmentId, Employee employee, String certType) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EntityNotFoundException("Equipment not found: " + equipmentId));
        // Validate certification
        certificationService.validateCertification(employee, certType);
        AssetAssignment assignment = new AssetAssignment();
        assignment.setEquipment(equipment);
        assignment.setEmployee(employee);
        assignment.setCheckoutTime(LocalDateTime.now());
        assignment.setStatus(AssetAssignment.Status.CHECKED_OUT);
        return assignmentRepository.save(assignment);
    }

    /**
     * Return equipment (check-in).
     */
    @Transactional
    public AssetAssignment returnEquipment(Long assignmentId) {
        AssetAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + assignmentId));
        assignment.setReturnTime(LocalDateTime.now());
        assignment.setStatus(AssetAssignment.Status.RETURNED);
        return assignmentRepository.save(assignment);
    }

    /**
     * Get all assignments.
     */
    public List<AssetAssignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    /**
     * Track overdue assignments (stub: filter in-memory, production: custom query).
     */
    public List<AssetAssignment> getOverdueAssignments() {
        return assignmentRepository.findAll().stream()
                .filter(a -> a.getStatus() == AssetAssignment.Status.CHECKED_OUT &&
                        a.getCheckoutTime().isBefore(LocalDateTime.now().minusDays(1)))
                .toList();
    }
}
