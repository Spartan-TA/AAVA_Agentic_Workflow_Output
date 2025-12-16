Section: E13-Integration Layer - WMS Department/Location Link (User Story 41)
Description: Integrate the system with the Warehouse Management System (WMS) to synchronize department and location data, ensuring consistency and reducing manual entry.
Design Specification:
- Package: com.company.integration.wms
- Entity: Department, Location (reused from domain)
- Service: WmsSyncService
- Repository: DepartmentRepository, LocationRepository
- Integration: REST client using Spring WebClient or RestTemplate
- Scheduler: Spring @Scheduled for periodic sync
- Error Handling: Retry logic, error logging
Sample Implementation:
@Service
public class WmsSyncService {
    @Scheduled(cron = "0 0 * * * *")
    public void syncDepartmentsAndLocations() {
        // Call WMS API, map response, update local entities
    }
}

Section: E13-Integration Layer - Expose OpenAPI Documentation (User Story 42)
Description: Provide OpenAPI (Swagger) documentation for all REST endpoints to facilitate integration and developer onboarding.
Design Specification:
- Package: com.company.config
- Dependency: springdoc-openapi-ui
- Configuration: OpenAPI bean, custom API info
- Security: Secure Swagger UI in production
Sample Implementation:
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info().title("API").version("v1"));
    }
}

Section: E14-Audit Trail - Log All Sensitive Changes (User Story 43)
Description: Track all sensitive changes (create, update, delete) to critical entities for compliance and traceability.
Design Specification:
- Package: com.company.audit
- Entity: AuditLog (id, entity, entityId, action, user, timestamp, details)
- Service: AuditLogService
- Aspect: Spring AOP for automatic logging
- Repository: AuditLogRepository
Sample Implementation:
@Aspect
@Component
public class AuditAspect {
    @AfterReturning(...)
    public void logChange(JoinPoint joinPoint) {
        // Persist AuditLog entity
    }
}

Section: E14-Audit Trail - Export Audit Logs (User Story 44)
Description: Allow authorized users to export audit logs for a given period in CSV or Excel format.
Design Specification:
- Controller: AuditLogExportController
- Service: AuditLogExportService
- Security: Role-based access (ADMIN/AUDITOR)
- Export: Apache POI for Excel, OpenCSV for CSV
Sample Implementation:
@GetMapping("/audit/export")
public void exportAuditLogs(@RequestParam ..., HttpServletResponse response) {
    // Write logs to response output stream
}

Section: E15-Reporting - Generate Attendance Reports (User Story 45)
Description: Generate detailed attendance reports for employees, filterable by date, department, and status.
Design Specification:
- Package: com.company.reporting.attendance
- Entity: AttendanceRecord (reused)
- Service: AttendanceReportService
- Controller: AttendanceReportController
- Export: PDF/Excel/CSV support
Sample Implementation:
@GetMapping("/reports/attendance")
public ResponseEntity<?> getAttendanceReport(...) {
    // Generate and return report
}
