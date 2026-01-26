package com.example.warehouse.scheduling.repository;

import com.example.warehouse.scheduling.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    // Find shift by name
    Shift findByName(String name);
}
