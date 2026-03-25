package com.warehouse.ems.repository;

import com.warehouse.ems.entity.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
}
