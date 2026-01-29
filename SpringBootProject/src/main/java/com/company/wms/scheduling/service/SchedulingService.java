package com.company.wms.scheduling.service;

import com.company.wms.scheduling.model.Schedule;
import com.company.wms.scheduling.model.ShiftTemplate;
import com.company.wms.scheduling.repository.ScheduleRepository;
import com.company.wms.scheduling.repository.ShiftTemplateRepository;
import com.company.wms.employee.model.Employee;
import com.company.wms.employee.repository.EmployeeRepository;
import com.company.wms.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for scheduling business logic.
 */
@Service
@RequiredArgsConstructor
public class SchedulingService {
    private final ScheduleRepository scheduleRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final EmployeeRepository employeeRepository;

    public List<ShiftTemplate> getAllShiftTemplates() {
        return shiftTemplateRepository.findAll();
    }

    public ShiftTemplate getShiftTemplateById(Long id) {
        return shiftTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift template not found with id: " + id));
    }

    public ShiftTemplate createShiftTemplate(ShiftTemplate template) {
        return shiftTemplateRepository.save(template);
    }

    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    public Schedule getScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
    }

    public List<Schedule> getSchedulesByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return scheduleRepository.findByEmployee(employee);
    }

    public List<Schedule> getSchedulesByDate(LocalDate date) {
        return scheduleRepository.findByDate(date);
    }

    public Schedule createSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public Schedule updateSchedule(Long id, Schedule updatedSchedule) {
        Schedule schedule = getScheduleById(id);
        schedule.setShiftTemplate(updatedSchedule.getShiftTemplate());
        schedule.setDate(updatedSchedule.getDate());
        return scheduleRepository.save(schedule);
    }

    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }
}
