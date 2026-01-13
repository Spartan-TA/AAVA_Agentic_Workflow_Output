package com.wms.scheduling.repository;

import com.wms.scheduling.entity.BlackoutDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository for BlackoutDate entity.
 */
@Repository
public interface BlackoutDateRepository extends JpaRepository<BlackoutDate, Long> {
    Optional<BlackoutDate> findByDate(LocalDate date);
}
