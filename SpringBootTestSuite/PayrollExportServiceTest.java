package com.warehouse.management.payroll;

import com.warehouse.management.payroll.PayrollExportService;
import org.junit.jupiter.api.*;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PayrollExportServiceTest {

    @Mock
    private PayrollRepository payrollRepository;

    @InjectMocks
    private PayrollExportService payrollExportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateExport_Valid() {
        when(payrollRepository.generateExport(any())).thenReturn("export.csv");
        String result = payrollExportService.generateExport(new java.util.Date());
        assertEquals("export.csv", result);
    }

    @Test
    void testDeliverViaSFTP_Valid() {
        doNothing().when(payrollRepository).deliverViaSFTP(anyString());
        assertDoesNotThrow(() -> payrollExportService.deliverViaSFTP("export.csv"));
    }

    @Test
    void testDeliverViaSFTP_Failure() {
        doThrow(new RuntimeException("SFTP error")).when(payrollRepository).deliverViaSFTP(anyString());
        assertThrows(RuntimeException.class, () -> payrollExportService.deliverViaSFTP("export.csv"));
    }
}