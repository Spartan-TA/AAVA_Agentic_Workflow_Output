package com.wms.attendance;

import com.wms.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for attendance management.
 */
@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRecordRepository attendanceRecordRepository;

    public AttendanceRecordDTO clockIn(Long employeeId, String deviceInfo) {
        AttendanceRecord record = AttendanceRecord.builder()
                .employeeId(employeeId)
                .clockIn(LocalDateTime.now())
                .deviceInfo(deviceInfo)
                .status("CLOCKED_IN")
                .createdAt(LocalDateTime.now())
                .build();
        return AttendanceRecordMapper.toDto(attendanceRecordRepository.save(record));
    }

    @Transactional
    public AttendanceRecordDTO clockOut(Long employeeId, String deviceInfo) {
        List<AttendanceRecord> records = attendanceRecordRepository.findByEmployeeId(employeeId);
        AttendanceRecord openRecord = records.stream()
                .filter(r -> r.getClockOut() == null)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No open clock-in found"));
        openRecord.setClockOut(LocalDateTime.now());
        openRecord.setStatus("CLOCKED_OUT");
        openRecord.setDeviceInfo(deviceInfo);
        return AttendanceRecordMapper.toDto(attendanceRecordRepository.save(openRecord));
    }

    public List<AttendanceRecordDTO> getRecords(Long employeeId) {
        return attendanceRecordRepository.findByEmployeeId(employeeId)
                .stream()
                .map(AttendanceRecordMapper::toDto)
                .toList();
    }
}