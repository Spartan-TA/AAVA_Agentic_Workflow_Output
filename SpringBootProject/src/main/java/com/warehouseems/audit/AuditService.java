package com.warehouseems.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuditService {
    public void logCreate(String entity, Object id, Object after) {
        log.info("AUDIT CREATE: {} id={} after={}", entity, id, after);
        // Persist to audit table (not shown for brevity)
    }
    public void logUpdate(String entity, Object id, Object before, Object after) {
        log.info("AUDIT UPDATE: {} id={} before={} after={}", entity, id, before, after);
        // Persist to audit table (not shown for brevity)
    }
    public void logDelete(String entity, Object id, Object before) {
        log.info("AUDIT DELETE: {} id={} before={}", entity, id, before);
        // Persist to audit table (not shown for brevity)
    }
}
