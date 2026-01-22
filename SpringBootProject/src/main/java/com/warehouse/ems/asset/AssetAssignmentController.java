package com.warehouse.ems.asset;

import com.warehouse.ems.employee.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for AssetAssignment endpoints.
 */
@RestController
@RequestMapping("/assets")
@Validated
public class AssetAssignmentController {
    private final AssetAssignmentService assignmentService;

    @Autowired
    public AssetAssignmentController(AssetAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    /**
     * Check out equipment to an employee.
     */
    @PostMapping("/checkout")
    public ResponseEntity<AssetAssignment> checkout(@RequestParam Long equipmentId,
                                                   @RequestParam Long employeeId,
                                                   @RequestParam String certType) {
        try {
            // In a real app, fetch Employee by employeeId
            Employee employee = new Employee();
            employee.setId(employeeId);
            AssetAssignment assignment = assignmentService.checkout(equipmentId, employee, certType);
            return new ResponseEntity<>(assignment, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Return equipment (check-in).
     */
    @PostMapping("/return")
    public ResponseEntity<AssetAssignment> returnEquipment(@RequestParam Long assignmentId) {
        try {
            AssetAssignment assignment = assignmentService.returnEquipment(assignmentId);
            return ResponseEntity.ok(assignment);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get all assignments.
     */
    @GetMapping("/assignments")
    public ResponseEntity<List<AssetAssignment>> getAllAssignments() {
        return ResponseEntity.ok(assignmentService.getAllAssignments());
    }
}
