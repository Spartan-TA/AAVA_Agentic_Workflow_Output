package com.warehouse.ems.scheduling;

import com.warehouse.ems.employee.Employee;
import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.exception.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Transactional
    public Schedule assignSchedule(Long employeeId, Long shiftId, LocalDate date) {
        // Conflict detection: Check if employee already has a schedule for this date
        List<Schedule> existing = scheduleRepository.findByEmployeeAndDate(employeeId, date);
        if (!existing.isEmpty()) {
            throw new ConflictException("Employee already has a schedule for this date.");
        }
        Schedule schedule = new Schedule();
        schedule.setEmployee(new Employee(employeeId));
        schedule.setShift(new ShiftTemplate(shiftId));
        schedule.setDate(date);
        schedule.setStatus("ASSIGNED");
        return scheduleRepository.save(schedule);
    }

    public List<Schedule> findConflictsByDate(LocalDate date) {
        return scheduleRepository.findConflictsByDate(date);
    }

    @Transactional
    public List<Schedule> bulkAssignSchedules(Set<Long> employeeIds, Long shiftId, LocalDate date) {
        // Bulk assignment: Assign shift to multiple employees
        List<Schedule> assigned = new java.util.ArrayList<>();
        for (Long employeeId : employeeIds) {
            try {
                assigned.add(assignSchedule(employeeId, shiftId, date));
            } catch (ConflictException e) {
                // Skip conflicting assignments
            }
        }
        return assigned;
    }

    @Transactional
    public Schedule updateScheduleStatus(Long scheduleId, String status, String conflict) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found."));
        schedule.setStatus(status);
        schedule.setConflict(conflict);
        return scheduleRepository.save(schedule);
    }
}
