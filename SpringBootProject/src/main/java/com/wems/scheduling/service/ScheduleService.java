package com.wems.scheduling.service;

import com.wems.scheduling.domain.*;
import com.wems.employee.domain.Employee;
import com.wems.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private ShiftTemplateRepository shiftTemplateRepository;

    public Schedule createSchedule(Long employeeId, Long shiftTemplateId, LocalDate date, String notes) {
        // Employee should be resolved from service
        Employee employee = null; // TODO: resolve employee
        ShiftTemplate template = shiftTemplateRepository.findById(shiftTemplateId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift template not found"));
        Schedule schedule = new Schedule();
        schedule.setEmployee(employee);
        schedule.setShiftTemplate(template);
        schedule.setScheduleDate(date);
        schedule.setStatus(ScheduleStatus.SCHEDULED);
        schedule.setNotes(notes);
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public List<Schedule> bulkAssign(List<Long> employeeIds, Long shiftTemplateId, LocalDate date) {
        ShiftTemplate template = shiftTemplateRepository.findById(shiftTemplateId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift template not found"));
        // Employees should be resolved from service
        List<Employee> employees = null; // TODO: resolve employees
        return employees.stream().map(emp -> {
            Schedule schedule = new Schedule();
            schedule.setEmployee(emp);
            schedule.setShiftTemplate(template);
            schedule.setScheduleDate(date);
            schedule.setStatus(ScheduleStatus.SCHEDULED);
            return scheduleRepository.save(schedule);
        }).toList();
    }

    public List<Schedule> getEmployeeSchedules(Long employeeId) {
        // Employee should be resolved from service
        Employee employee = null; // TODO: resolve employee
        return scheduleRepository.findAll(); // Replace with proper query
    }

    public void cancelSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
        schedule.setStatus(ScheduleStatus.CANCELLED);
        scheduleRepository.save(schedule);
    }
}
