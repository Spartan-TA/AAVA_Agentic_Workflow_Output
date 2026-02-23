package com.example.repository;

import com.example.entity.AuditLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AuditLogRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void save_andFindById() {
        AuditLog log = new AuditLog();
        log.setAction("CREATE_EMPLOYEE");
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);

        assertTrue(auditLogRepository.findById(log.getId()).isPresent());
    }
}