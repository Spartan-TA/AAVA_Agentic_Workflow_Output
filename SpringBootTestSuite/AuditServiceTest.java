@SpringBootTest
public class AuditServiceTest {
    @MockBean private AuditRepository auditRepository;
    @Autowired private AuditService auditService;

    @Test
    void testLogAuditEntry_ValidInput_Success() {
        AuditEntryDto dto = new AuditEntryDto("EMPLOYEE_CREATE", "Created employee", 1L);
        when(auditRepository.save(any(AuditEntry.class))).thenReturn(new AuditEntry());
        AuditEntry result = auditService.logAuditEntry(dto);
        assertNotNull(result);
    }

    @Test
    void testGetAuditLogs_ValidInput_Success() {
        AuditFilter filter = new AuditFilter();
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditEntry> page = new PageImpl<>(List.of(new AuditEntry()));
        when(auditRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<AuditEntry> result = auditService.getAuditLogs(filter, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testExportAuditLogs_ValidInput_Success() {
        List<AuditEntry> logs = List.of(new AuditEntry());
        when(auditRepository.findByDateRange(any(), any())).thenReturn(logs);
        byte[] csv = auditService.exportAuditLogs(LocalDate.now().minusDays(7), LocalDate.now());
        assertNotNull(csv);
    }
}