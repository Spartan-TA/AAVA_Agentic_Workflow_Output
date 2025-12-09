import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;
import java.time.*;

public class ShiftTemplateServiceTest {
    @Mock
    private ShiftTemplateRepository mockRepository;
    @Mock
    private EmployeeRepository mockEmployeeRepository;
    @InjectMocks
    private ShiftTemplateService service;
    private AutoCloseable closeable;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testCreateShiftTemplate_ValidInput() {
        ShiftTemplateDTO dto = new ShiftTemplateDTO("Morning", LocalDateTime.of(2024,6,1,8,0), LocalDateTime.of(2024,6,1,16,0));
        when(mockRepository.save(any())).thenReturn(new ShiftTemplate(1L, "Morning", dto.getStart(), dto.getEnd()));
        ShiftTemplate result = service.createShiftTemplate(dto);
        assertNotNull(result);
        assertEquals("Morning", result.getName());
    }

    @Test
    public void testCreateShiftTemplate_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> service.createShiftTemplate(null));
    }

    @Test
    public void testCreateShiftTemplate_DuplicateName() {
        ShiftTemplateDTO dto = new ShiftTemplateDTO("Night", LocalDateTime.of(2024,6,1,22,0), LocalDateTime.of(2024,6,2,6,0));
        when(mockRepository.findByName("Night")).thenReturn(Optional.of(new ShiftTemplate(2L, "Night", dto.getStart(), dto.getEnd())));
        assertThrows(DuplicateShiftTemplateException.class, () -> service.createShiftTemplate(dto));
    }

    @Test
    public void testCreateShiftTemplate_InvalidTimeRange() {
        ShiftTemplateDTO dto = new ShiftTemplateDTO("Invalid", LocalDateTime.of(2024,6,1,16,0), LocalDateTime.of(2024,6,1,8,0));
        assertThrows(InvalidTimeRangeException.class, () -> service.createShiftTemplate(dto));
    }

    @Test
    public void testGetShiftTemplateById_ValidId() {
        when(mockRepository.findById(1L)).thenReturn(Optional.of(new ShiftTemplate(1L, "Morning", LocalDateTime.of(2024,6,1,8,0), LocalDateTime.of(2024,6,1,16,0))));
        ShiftTemplate result = service.getShiftTemplateById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void testGetShiftTemplateById_NullId() {
        assertThrows(IllegalArgumentException.class, () -> service.getShiftTemplateById(null));
    }

    @Test
    public void testUpdateShiftTemplate_ValidUpdate() {
        ShiftTemplateDTO dto = new ShiftTemplateDTO("Updated", LocalDateTime.of(2024,6,1,9,0), LocalDateTime.of(2024,6,1,17,0));
        when(mockRepository.findById(1L)).thenReturn(Optional.of(new ShiftTemplate(1L, "Morning", LocalDateTime.of(2024,6,1,8,0), LocalDateTime.of(2024,6,1,16,0))));
        when(mockRepository.save(any())).thenReturn(new ShiftTemplate(1L, "Updated", dto.getStart(), dto.getEnd()));
        ShiftTemplate result = service.updateShiftTemplate(1L, dto);
        assertEquals("Updated", result.getName());
    }

    @Test
    public void testDeleteShiftTemplate_ValidId() {
        when(mockRepository.existsById(1L)).thenReturn(true);
        assertDoesNotThrow(() -> service.deleteShiftTemplate(1L));
        verify(mockRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testListShiftTemplates_EmptyList() {
        when(mockRepository.findAll()).thenReturn(Collections.emptyList());
        List<ShiftTemplate> result = service.listShiftTemplates();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testAssignShiftToEmployee_ValidAssignment() {
        when(mockRepository.findById(1L)).thenReturn(Optional.of(new ShiftTemplate(1L, "Morning", LocalDateTime.of(2024,6,1,8,0), LocalDateTime.of(2024,6,1,16,0))));
        when(mockEmployeeRepository.findById(100L)).thenReturn(Optional.of(new Employee(100L, "John Doe")));
        assertDoesNotThrow(() -> service.assignShiftToEmployee(1L, 100L));
    }

    @Test
    public void testDetectConflicts_BoundaryTimes() {
        when(mockRepository.findShiftsForEmployee(100L, LocalDateTime.of(2024,6,1,8,0), LocalDateTime.of(2024,6,1,16,0)))
            .thenReturn(Collections.singletonList(new ShiftTemplate(1L, "Morning", LocalDateTime.of(2024,6,1,8,0), LocalDateTime.of(2024,6,1,16,0))));
        boolean conflict = service.detectConflicts(100L, LocalDateTime.of(2024,6,1,8,0), LocalDateTime.of(2024,6,1,16,0));
        assertTrue(conflict);
    }
}
