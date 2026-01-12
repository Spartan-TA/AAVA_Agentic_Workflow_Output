package com.warehouseems.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployee_IdAndTimestampBetween(Long employeeId, LocalDate start, LocalDate end);
}
