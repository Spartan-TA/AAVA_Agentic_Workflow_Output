package com.wms.ems.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface WarehouseCalendarRepository extends JpaRepository<WarehouseCalendar, Long> {
    boolean existsByDateAndType(LocalDate date, String type);
}
