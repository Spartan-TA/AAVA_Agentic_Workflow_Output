package com.warehouse.management.repository;

import com.warehouse.management.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    // Additional query methods can be defined here
}
