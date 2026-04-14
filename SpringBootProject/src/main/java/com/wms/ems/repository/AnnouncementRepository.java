package com.wms.ems.repository;

import com.wms.ems.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Announcement entity operations.
 * Provides CRUD and custom query methods for announcement management.
 */
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    /**
     * Find active announcements within a date range.
     * @param currentDate the current date
     * @return List of active Announcements
     */
    @Query("SELECT a FROM Announcement a WHERE a.startDate <= :currentDate AND a.endDate >= :currentDate AND a.isActive = true")
    List<Announcement> findActiveAnnouncements(@Param("currentDate") LocalDate currentDate);
}
