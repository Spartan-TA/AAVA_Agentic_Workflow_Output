package com.company.wems.attendance;

import com.company.wems.employee.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    /**
     * Endpoint for employee clock-in.
     */
    @PostMapping("/clock-in")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AttendanceEvent> clockIn(@RequestBody ClockInRequest request, Principal principal) {
        // Assume Employee is fetched from principal
        Employee employee = getEmployeeFromPrincipal(principal);
        AttendanceEvent event = attendanceService.clockIn(employee, request.getDevice(), request.getLocation());
        return ResponseEntity.ok(event);
    }

    /**
     * Endpoint for employee clock-out.
     */
    @PostMapping("/clock-out")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AttendanceEvent> clockOut(Principal principal) {
        Employee employee = getEmployeeFromPrincipal(principal);
        AttendanceEvent event = attendanceService.clockOut(employee);
        return ResponseEntity.ok(event);
    }

    /**
     * Endpoint to view attendance history.
     */
    @GetMapping("/history/{employeeId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER')")
    public ResponseEntity<List<AttendanceEvent>> getHistory(@PathVariable Long employeeId) {
        List<AttendanceEvent> history = attendanceService.getAttendanceHistory(employeeId);
        return ResponseEntity.ok(history);
    }

    // DTO for clock-in request
    public static class ClockInRequest {
        private String device;
        private String location;
        public String getDevice() { return device; }
        public void setDevice(String device) { this.device = device; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    // Helper method to get Employee from Principal (stub)
    private Employee getEmployeeFromPrincipal(Principal principal) {
        // TODO: Implement actual lookup
        return new Employee();
    }
}
