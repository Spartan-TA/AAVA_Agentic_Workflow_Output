package SpringBootTestSuite;

import com.example.warehouse.payroll.PayrollExport;
import com.example.warehouse.payroll.PayrollService;
import com.example.warehouse.payroll.PayrollRepository;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PayrollServiceTest {
    @Mock
    private PayrollRepository payrollRepository;

    @InjectMocks
    private PayrollService payrollService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void generatePayrollExport_ValidInput_ReturnsPayrollExport() {
        PayrollExport export = new PayrollExport();
        export.setExportDate(LocalDate.now());
        when(payrollRepository.save(any())).thenReturn(export);
        PayrollExport result = payrollService.generatePayrollExport(LocalDate.now());
        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getExportDate());
    }

    @Test
    public void generatePayrollExport_NullDate_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> payrollService.generatePayrollExport(null));
    }

    @Test
    public void getPayrollExportById_ValidId_ReturnsPayrollExport() {
        PayrollExport export = new PayrollExport();
        export.setId(1L);
        when(payrollRepository.findById(1L)).thenReturn(Optional.of(export));
        PayrollExport result = payrollService.getPayrollExportById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void getPayrollExportById_InvalidId_ThrowsResourceNotFoundException() {
        when(payrollRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> payrollService.getPayrollExportById(99L));
    }

    @Test
    public void getAllPayrollExports_ReturnsList() {
        PayrollExport export = new PayrollExport();
        export.setId(1L);
        when(payrollRepository.findAll()).thenReturn(Collections.singletonList(export));
        List<PayrollExport> result = payrollService.getAllPayrollExports();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void getAllPayrollExports_Empty_ReturnsEmptyList() {
        when(payrollRepository.findAll()).thenReturn(Collections.emptyList());
        List<PayrollExport> result = payrollService.getAllPayrollExports();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
