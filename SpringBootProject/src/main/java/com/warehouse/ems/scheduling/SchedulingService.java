package com.warehouse.ems.scheduling;

import com.warehouse.ems.employee.Employee;
import com.warehouse.ems.employee.EmployeeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SchedulingService {
    @Autowired
    private ShiftRepository shiftRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public Shift createShift(Shift shift) {
        return shiftRepository.save(shift);
    }

    @Transactional
    public Schedule assignShift(Long employeeId, Long shiftId, LocalDate date) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        Schedule schedule = Schedule.builder()
                .employee(employee)
                .shift(shift)
                .scheduledDate(date)
                .build();
        return scheduleRepository.save(schedule);
    }

    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    public List<Schedule> getSchedulesForEmployee(Long employeeId) {
        return scheduleRepository.findAll().stream()
                .filter(s -> s.getEmployee().getId().equals(employeeId))
                .toList();
    }

    public Optional<Schedule> getSchedule(Long id) {
        return scheduleRepository.findById(id);
    }
}
