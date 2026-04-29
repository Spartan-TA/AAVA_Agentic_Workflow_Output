package com.example.ems.repository;

import com.example.ems.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    // Custom query methods if needed
}
