package com.example.warehouse.service;

import com.example.warehouse.entity.Schedule;
import com.example.warehouse.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for Schedule operations.
 */
@Service
public class ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;

    public List<Schedule> getSchedulesForEmployee(Long employeeId, LocalDate start, LocalDate end) {
        return scheduleRepository.findByEmployeeAndDateRange(employeeId, start, end);
    }

    public List<Schedule> getSchedulesByDate(LocalDate date) {
        return scheduleRepository.findByDate(date);
    }

    @Transactional
    public Schedule assignShift(Schedule schedule) {
        // Conflict detection: check if employee already has a schedule for this date
        List<Schedule> existing = scheduleRepository.findByEmployeeAndDateRange(schedule.getEmployee().getId(), schedule.getDate(), schedule.getDate());
        if (!existing.isEmpty()) {
            throw new IllegalStateException("Employee already assigned to a shift on this date.");
        }
        return scheduleRepository.save(schedule);
    }
}
