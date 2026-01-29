package com.company.wms.scheduling.repository;

import com.company.wms.scheduling.model.Schedule;
import com.company.wms.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Schedule entity.
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByEmployee(Employee employee);
    List<Schedule> findByDate(LocalDate date);
}
