package SpringBootTestSuite;

import com.example.warehouse.audit.AuditAspect;
import com.example.warehouse.model.AuditLog;
import com.example.warehouse.repository.AuditLogRepository;
import com.example.warehouse.service.EmployeeService;
import com.example.warehouse.model.Employee;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditAspect.
 * Tests audit logging for create, update, and delete service methods.
 */
@ExtendWith(MockitoExtension.class)
class AuditAspectTest {
    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private EmployeeService employeeService;
    @InjectMocks
    private AuditAspect auditAspect;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setBadgeId("BADGE123");
        employee.setRole("WORKER");
        employee.setDepartment("Shipping");
        employee.setStatus("ACTIVE");
        employee.setCreatedAt(LocalDateTime.now());
        employee.setDeletedAt(null);
    }

    @Test
    void testLogAfterService_CreateMethod_CreatesAuditLog() {
        AuditLog log = new AuditLog();
        log.setEntityType("Employee");
        log.setEntityId(employee.getId());
        log.setAction("CREATE");
        log.setActor("admin");
        log.setTimestamp(LocalDateTime.now());
        log.setBeforeState(null);
        log.setAfterState(employee.toString());
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(log);
        auditAspect.logAfterService("CREATE", employee, null, "admin");
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void testLogAfterService_UpdateMethod_CreatesAuditLog() {
        Employee before = new Employee();
        before.setId(1L);
        before.setName("Old Name");
        AuditLog log = new AuditLog();
        log.setEntityType("Employee");
        log.setEntityId(employee.getId());
        log.setAction("UPDATE");
        log.setActor("admin");
        log.setTimestamp(LocalDateTime.now());
        log.setBeforeState(before.toString());
        log.setAfterState(employee.toString());
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(log);
        auditAspect.logAfterService("UPDATE", employee, before, "admin");
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void testLogAfterService_DeleteMethod_CreatesAuditLog() {
        AuditLog log = new AuditLog();
        log.setEntityType("Employee");
        log.setEntityId(employee.getId());
        log.setAction("DELETE");
        log.setActor("admin");
        log.setTimestamp(LocalDateTime.now());
        log.setBeforeState(employee.toString());
        log.setAfterState(null);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(log);
        auditAspect.logAfterService("DELETE", null, employee, "admin");
        verify(auditLogRepository).save(any(AuditLog.class));
    }
}
