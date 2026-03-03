package com.wms.ems.payroll.service;

import com.wms.ems.payroll.repository.PayrollExportRepository;
import com.wms.ems.attendance.repository.AttendanceEventRepository;
import com.wms.ems.leave.repository.LeaveRequestRepository;
import com.wms.ems.payroll.entity.PayrollExport;
import com.wms.ems.payroll.enums.ExportStatus;
import com.wms.ems.attendance.entity.AttendanceEvent;
import com.wms.ems.leave.entity.LeaveRequest;
import com.wms.ems.common.exception.ResourceNotFoundException;
import com.wms.ems.common.exception.ValidationException;
import com.wms.ems.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing payroll exports.
 */
@Service
@Transactional
@Slf4j
public class PayrollService {

    @Autowired
    private PayrollExportRepository payrollExportRepository;

    @Autowired
    private AttendanceEventRepository attendanceEventRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    /**
     * Generates a payroll export by aggregating attendance and leave data.
     * @param startDate Start date
     * @param endDate End date
     * @return PayrollExport
     */
    public PayrollExport generateExport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            log.error("Invalid date range for payroll export");
            throw new ValidationException("Invalid date range");
        }
        List<AttendanceEvent> attendance = attendanceEventRepository.findByEventDateBetween(startDate, endDate);
        List<LeaveRequest> leaves = leaveRequestRepository.findByStartDateBetween(startDate, endDate);
        PayrollExport export = new PayrollExport();
        export.setStartDate(startDate);
        export.setEndDate(endDate);
        export.setStatus(ExportStatus.PENDING);
        export.setAttendanceEvents(attendance);
        export.setLeaveRequests(leaves);
        try {
            PayrollExport saved = payrollExportRepository.save(export);
            log.info("Payroll export generated: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Failed to generate payroll export", e);
            throw new BusinessException("Failed to generate payroll export");
        }
    }

    /**
     * Updates the status of a payroll export.
     * @param exportId Export ID
     * @param status ExportStatus
     * @return PayrollExport
     */
    public PayrollExport updateExportStatus(Long exportId, ExportStatus status) {
        PayrollExport export = payrollExportRepository.findById(exportId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll export not found"));
        export.setStatus(status);
        try {
            PayrollExport updated = payrollExportRepository.save(export);
            log.info("Payroll export {} status updated to {}", exportId, status);
            return updated;
        } catch (Exception e) {
            log.error("Failed to update payroll export status", e);
            throw new BusinessException("Failed to update payroll export status");
        }
    }

    /**
     * Gets payroll exports by status.
     * @param status ExportStatus
     * @return List of PayrollExport
     */
    @Transactional(readOnly = true)
    public List<PayrollExport> getExportsByStatus(ExportStatus status) {
        try {
            return payrollExportRepository.findByStatus(status);
        } catch (Exception e) {
            log.error("Failed to fetch payroll exports by status", e);
            throw new BusinessException("Failed to fetch payroll exports by status");
        }
    }

    /**
     * Gets payroll exports by date range.
     * @param start Start date
     * @param end End date
     * @return List of PayrollExport
     */
    @Transactional(readOnly = true)
    public List<PayrollExport> getExportsByDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            log.error("Invalid date range for payroll export");
            throw new ValidationException("Invalid date range");
        }
        try {
            return payrollExportRepository.findByStartDateGreaterThanEqualAndEndDateLessThanEqual(start, end);
        } catch (Exception e) {
            log.error("Failed to fetch payroll exports by date range", e);
            throw new BusinessException("Failed to fetch payroll exports by date range");
        }
    }

    /**
     * Generates payroll file content (CSV).
     * @param exportId Export ID
     * @return String (CSV content)
     */
    @Transactional(readOnly = true)
    public String generatePayrollFile(Long exportId) {
        PayrollExport export = payrollExportRepository.findById(exportId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll export not found"));
        StringBuilder sb = new StringBuilder();
        sb.append("EmployeeID,Date,Hours,Type
");
        for (AttendanceEvent event : export.getAttendanceEvents()) {
            sb.append(event.getEmployee().getId()).append(",")
              .append(event.getEventDate()).append(",")
              .append(event.getHours()).append(",Attendance
");
        }
        for (LeaveRequest leave : export.getLeaveRequests()) {
            sb.append(leave.getEmployee().getId()).append(",")
              .append(leave.getStartDate()).append(",")
              .append(leave.getDuration()).append(",Leave
");
        }
        log.info("Payroll file generated for export {}", exportId);
        return sb.toString();
    }
}
