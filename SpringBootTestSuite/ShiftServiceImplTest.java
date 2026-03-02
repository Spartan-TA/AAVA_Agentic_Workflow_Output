import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ShiftServiceImplTest {
    @Mock
    private ShiftRepository shiftRepository;
    @InjectMocks
    private ShiftServiceImpl shiftService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("testCreateShift_ValidInput_ReturnsShift")
    void testCreateShift_ValidInput_ReturnsShift() {
        Shift shift = new Shift("Morning", LocalDateTime.now(), LocalDateTime.now().plusHours(8));
        when(shiftRepository.save(any(Shift.class))).thenReturn(shift);
        Shift result = shiftService.createShift(shift);
        assertEquals(shift, result);
    }

    @Test
    @DisplayName("testCreateShift_NullInput_ThrowsIllegalArgumentException")
    void testCreateShift_NullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> shiftService.createShift(null));
    }

    @Test
    @DisplayName("testGetShiftById_ValidId_ReturnsShift")
    void testGetShiftById_ValidId_ReturnsShift() {
        Shift shift = new Shift("Morning", LocalDateTime.now(), LocalDateTime.now().plusHours(8));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        Shift result = shiftService.getShiftById(1L);
        assertEquals(shift, result);
    }

    @Test
    @DisplayName("testGetShiftById_InvalidId_ThrowsShiftNotFoundException")
    void testGetShiftById_InvalidId_ThrowsShiftNotFoundException() {
        when(shiftRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ShiftNotFoundException.class, () -> shiftService.getShiftById(99L));
    }

    @Test
    @DisplayName("testGetAllShifts_ReturnsShiftList")
    void testGetAllShifts_ReturnsShiftList() {
        List<Shift> shifts = Arrays.asList(new Shift("Morning", LocalDateTime.now(), LocalDateTime.now().plusHours(8)));
        when(shiftRepository.findAll()).thenReturn(shifts);
        List<Shift> result = shiftService.getAllShifts();
        assertEquals(shifts, result);
    }

    @Test
    @DisplayName("testUpdateShift_ValidInput_UpdatesShift")
    void testUpdateShift_ValidInput_UpdatesShift() {
        Shift shift = new Shift("Morning", LocalDateTime.now(), LocalDateTime.now().plusHours(8));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(shiftRepository.save(any(Shift.class))).thenReturn(shift);
        Shift updated = shiftService.updateShift(1L, shift);
        assertEquals(shift, updated);
    }

    @Test
    @DisplayName("testUpdateShift_NullInput_ThrowsIllegalArgumentException")
    void testUpdateShift_NullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> shiftService.updateShift(1L, null));
    }

    @Test
    @DisplayName("testDeleteShift_ValidId_DeletesShift")
    void testDeleteShift_ValidId_DeletesShift() {
        Shift shift = new Shift("Morning", LocalDateTime.now(), LocalDateTime.now().plusHours(8));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        doNothing().when(shiftRepository).delete(shift);
        shiftService.deleteShift(1L);
        verify(shiftRepository).delete(shift);
    }

    @Test
    @DisplayName("testDeleteShift_InvalidId_ThrowsShiftNotFoundException")
    void testDeleteShift_InvalidId_ThrowsShiftNotFoundException() {
        when(shiftRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ShiftNotFoundException.class, () -> shiftService.deleteShift(99L));
    }

    @Test
    @DisplayName("testCreateRecurringShift_ValidInput_ReturnsShifts")
    void testCreateRecurringShift_ValidInput_ReturnsShifts() {
        List<Shift> recurringShifts = Arrays.asList(new Shift("Morning", LocalDateTime.now(), LocalDateTime.now().plusHours(8)));
        when(shiftRepository.saveAll(anyList())).thenReturn(recurringShifts);
        List<Shift> result = shiftService.createRecurringShift(recurringShifts);
        assertEquals(recurringShifts, result);
    }

    @Test
    @DisplayName("testCreateRecurringShift_EmptyList_ReturnsEmptyList")
    void testCreateRecurringShift_EmptyList_ReturnsEmptyList() {
        when(shiftRepository.saveAll(Collections.emptyList())).thenReturn(Collections.emptyList());
        List<Shift> result = shiftService.createRecurringShift(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("testCreateShift_OverlappingShift_ThrowsConflictException")
    void testCreateShift_OverlappingShift_ThrowsConflictException() {
        Shift shift = new Shift("Morning", LocalDateTime.now(), LocalDateTime.now().plusHours(8));
        when(shiftRepository.findOverlappingShifts(any(), any())).thenReturn(Arrays.asList(shift));
        assertThrows(ShiftConflictException.class, () -> shiftService.createShift(shift));
    }

    @Test
    @DisplayName("testCreateShift_EmptyName_ThrowsValidationException")
    void testCreateShift_EmptyName_ThrowsValidationException() {
        Shift shift = new Shift("", LocalDateTime.now(), LocalDateTime.now().plusHours(8));
        assertThrows(ValidationException.class, () -> shiftService.createShift(shift));
    }

    @AfterEach
    void tearDown() {
        // Cleanup if needed
    }
}
