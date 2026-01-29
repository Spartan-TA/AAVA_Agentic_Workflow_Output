package com.warehouse.employee.management.attendance.service;

import com.warehouse.employee.management.attendance.domain.Attendance;
import com.warehouse.employee.management.attendance.repository.AttendanceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {
    @Autowired
    private AttendanceRepo attendanceRepo;

    public List<Attendance> getAllAttendance() {
        return attendanceRepo.findAll();
    }

    public Optional<Attendance> getAttendanceById(Long id) {
        return attendanceRepo.findById(id);
    }

    public Attendance saveAttendance(Attendance attendance) {
        return attendanceRepo.save(attendance);
    }

    public void deleteAttendance(Long id) {
        attendanceRepo.deleteById(id);
    }
}
