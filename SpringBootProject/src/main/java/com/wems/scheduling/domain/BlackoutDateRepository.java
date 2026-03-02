package com.wems.scheduling.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackoutDateRepository extends JpaRepository<BlackoutDate, Long> {
}
