package SpringBootTestSuite;

import com.example.warehouse.shift.ShiftTemplate;
import com.example.warehouse.shift.ShiftAssignment;
import com.example.warehouse.shift.ShiftService;
import com.example.warehouse.shift.ShiftRepository;
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
public class ShiftServiceTest {
    @Mock
    private ShiftRepository shiftRepository;

    @InjectMocks
    private ShiftService shiftService;

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
    public void createShiftTemplate_ValidInput_ReturnsShiftTemplate() {
        ShiftTemplate template = new ShiftTemplate();
        template.setName("Morning Shift");
        when(shiftRepository.save(any())).thenReturn(template);
        ShiftTemplate result = shiftService.createShiftTemplate(template);
        assertNotNull(result);
        assertEquals("Morning Shift", result.getName());
    }

    @Test
    public void createShiftTemplate_NullInput_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> shiftService.createShiftTemplate(null));
    }

    @Test
    public void getShiftTemplateById_ValidId_ReturnsShiftTemplate() {
        ShiftTemplate template = new ShiftTemplate();
        template.setId(1L);
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(template));
        ShiftTemplate result = shiftService.getShiftTemplateById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void getShiftTemplateById_InvalidId_ThrowsResourceNotFoundException() {
        when(shiftRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> shiftService.getShiftTemplateById(99L));
    }

    @Test
    public void assignShift_ValidInput_ReturnsShiftAssignment() {
        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setEmployeeId(1L);
        assignment.setShiftDate(LocalDate.now());
        when(shiftRepository.saveAssignment(any())).thenReturn(assignment);
        ShiftAssignment result = shiftService.assignShift(1L, 1L, LocalDate.now());
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
    }

    @Test
    public void assignShift_NullEmployeeId_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> shiftService.assignShift(null, 1L, LocalDate.now()));
    }

    @Test
    public void assignShift_NullShiftId_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> shiftService.assignShift(1L, null, LocalDate.now()));
    }

    @Test
    public void assignShift_NullDate_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> shiftService.assignShift(1L, 1L, null));
    }

    @Test
    public void getAllShiftTemplates_ReturnsList() {
        ShiftTemplate template = new ShiftTemplate();
        template.setId(1L);
        when(shiftRepository.findAll()).thenReturn(Collections.singletonList(template));
        List<ShiftTemplate> result = shiftService.getAllShiftTemplates();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void getAllShiftTemplates_Empty_ReturnsEmptyList() {
        when(shiftRepository.findAll()).thenReturn(Collections.emptyList());
        List<ShiftTemplate> result = shiftService.getAllShiftTemplates();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
