package com.wms.attendance.service;

import com.wms.attendance.dto.AttendanceEventDto;
import com.wms.attendance.domain.AttendanceEvent;
import com.wms.attendance.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AttendanceService interface.
 * Handles business logic for attendance management.
 */
@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Override
    public AttendanceEventDto recordAttendance(AttendanceEventDto attendanceEventDto) {
        AttendanceEvent event = new AttendanceEvent(
            attendanceEventDto.getEmployeeId(),
            attendanceEventDto.getEventType(),
            attendanceEventDto.getEventTimestamp()
        );
        AttendanceEvent saved = attendanceRepository.save(event);
        return new AttendanceEventDto(saved);
    }

    @Override
    public List<AttendanceEventDto> getAttendanceByEmployeeId(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId).stream()
            .map(AttendanceEventDto::new)
            .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceEventDto> getAllAttendanceEvents() {
        return attendanceRepository.findAll().stream()
            .map(AttendanceEventDto::new)
            .collect(Collectors.toList());
    }
}
