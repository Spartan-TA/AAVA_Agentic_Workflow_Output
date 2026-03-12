package SpringBootTestSuite;

import com.example.warehouse.reporting.ReportingService;
import com.example.warehouse.reporting.ReportingRepository;
import com.example.warehouse.reporting.Report;
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
public class ReportingServiceTest {
    @Mock
    private ReportingRepository reportingRepository;

    @InjectMocks
    private ReportingService reportingService;

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
    public void generateReport_ValidInput_ReturnsReport() {
        Report report = new Report();
        report.setType("Attendance");
        report.setGeneratedDate(LocalDate.now());
        when(reportingRepository.save(any())).thenReturn(report);
        Report result = reportingService.generateReport("Attendance", LocalDate.now(), LocalDate.now().plusDays(7));
        assertNotNull(result);
        assertEquals("Attendance", result.getType());
    }

    @Test
    public void generateReport_NullType_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> reportingService.generateReport(null, LocalDate.now(), LocalDate.now().plusDays(7)));
    }

    @Test
    public void generateReport_InvalidDates_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> reportingService.generateReport("Attendance", LocalDate.now().plusDays(7), LocalDate.now()));
    }

    @Test
    public void getReportById_ValidId_ReturnsReport() {
        Report report = new Report();
        report.setId(1L);
        when(reportingRepository.findById(1L)).thenReturn(Optional.of(report));
        Report result = reportingService.getReportById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void getReportById_InvalidId_ThrowsResourceNotFoundException() {
        when(reportingRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> reportingService.getReportById(99L));
    }

    @Test
    public void getAllReports_ReturnsList() {
        Report report = new Report();
        report.setId(1L);
        when(reportingRepository.findAll()).thenReturn(Collections.singletonList(report));
        List<Report> result = reportingService.getAllReports();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void getAllReports_Empty_ReturnsEmptyList() {
        when(reportingRepository.findAll()).thenReturn(Collections.emptyList());
        List<Report> result = reportingService.getAllReports();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
