@SpringBootTest
public class PayrollServiceTest {
    @MockBean private PayrollRepository payrollRepository;
    @MockBean private AttendanceRepository attendanceRepository;
    @Autowired private PayrollService payrollService;

    @Test
    void testGeneratePayrollExport_ValidInput_Success() {
        List<PayrollRecord> records = List.of(new PayrollRecord());
        when(payrollRepository.generateExport(any(), any())).thenReturn(records);

        List<PayrollRecord> result = payrollService.generatePayrollExport(LocalDate.now().minusDays(14), LocalDate.now());

        assertEquals(1, result.size());
    }

    @Test
    void testDeliverPayrollFile_ValidInput_Success() {
        doNothing().when(payrollRepository).deliverFile(anyString(), any(PayrollProvider.class));
        assertDoesNotThrow(() -> payrollService.deliverPayrollFile("file.csv", PayrollProvider.ADP));
    }

    @Test
    void testReconcilePayroll_ValidInput_Success() {
        when(payrollRepository.findById(1L)).thenReturn(Optional.of(new PayrollExport()));
        doNothing().when(payrollRepository).reconcile(any(PayrollExport.class), anyList());
        assertDoesNotThrow(() -> payrollService.reconcilePayroll(1L));
    }
}