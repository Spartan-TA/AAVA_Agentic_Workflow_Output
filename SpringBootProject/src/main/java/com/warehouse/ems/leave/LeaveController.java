package com.warehouse.ems.leave;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @Autowired
    private LeaveService leaveService;

    @Operation(summary = "Request leave", description = "Employee requests leave")
    @ApiResponse(responseCode = "200", description = "Leave request submitted")
    @PostMapping("/request")
    public ResponseEntity<LeaveDTO> requestLeave(@RequestParam Long employeeId,
                                                @RequestParam String type,
                                                @RequestParam String startDate,
                                                @RequestParam String endDate) {
        LeaveRequest leave = leaveService.requestLeave(employeeId, type, LocalDate.parse(startDate), LocalDate.parse(endDate));
        LeaveDTO dto = toDTO(leave);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Approve leave", description = "Approver approves leave request")
    @ApiResponse(responseCode = "200", description = "Leave approved")
    @PutMapping("/approve")
    public ResponseEntity<LeaveDTO> approveLeave(@RequestParam Long leaveId,
                                                @RequestParam Long approverId) {
        LeaveRequest leave = leaveService.approveLeave(leaveId, approverId);
        LeaveDTO dto = toDTO(leave);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Get leave balance", description = "Returns leave balance for employee and type")
    @ApiResponse(responseCode = "200", description = "Leave balance fetched")
    @GetMapping("/balance")
    public ResponseEntity<LeaveDTO> getLeaveBalance(@RequestParam Long employeeId,
                                                   @RequestParam String type) {
        LeaveBalance balance = leaveService.getLeaveBalance(employeeId, type);
        LeaveDTO dto = new LeaveDTO();
        dto.setEmployeeId(employeeId);
        dto.setType(type);
        dto.setBalance(balance != null ? balance.getBalance() : 0.0);
        return ResponseEntity.ok(dto);
    }

    // Utility method to convert LeaveRequest to DTO
    private LeaveDTO toDTO(LeaveRequest leave) {
        LeaveDTO dto = new LeaveDTO();
        dto.setId(leave.getId());
        dto.setEmployeeId(leave.getEmployee().getId());
        dto.setType(leave.getType());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setStatus(leave.getStatus());
        dto.setApproverId(leave.getApprover() != null ? leave.getApprover().getId() : null);
        dto.setBalance(leave.getBalance());
        return dto;
    }
}
