import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ShiftServiceTest {
    @Mock
    private ShiftRepository shiftRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ShiftService shiftService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateShift_Valid() {
        Shift shift = new Shift();
        shift.setName("Morning Shift");
        shift.setStartTime(LocalTime.of(8, 0));
        shift.setEndTime(LocalTime.of(16, 0));
        when(shiftRepository.save(any(Shift.class))).thenReturn(shift);
        Shift result = shiftService.createShift(shift);
        assertNotNull(result);
        assertEquals("Morning Shift", result.getName());
        verify(shiftRepository, times(1)).save(shift);
    }

    @Test
    void testCreateShift_NullInput() {
        assertThrows(ValidationException.class, () -> shiftService.createShift(null));
    }

    @Test
    void testCreateShift_InvalidTimeRange() {
        Shift shift = new Shift();
        shift.setStartTime(LocalTime.of(16, 0));
        shift.setEndTime(LocalTime.of(8, 0));
        assertThrows(ValidationException.class, () -> shiftService.createShift(shift));
    }

    @Test
    void testAssignEmployeeToShift_Valid() {
        Shift shift = new Shift();
        shift.setId(1L);
        Employee employee = new Employee();
        employee.setId(1L);
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRepository.save(any(Shift.class))).thenReturn(shift);
        Shift result = shiftService.assignEmployeeToShift(1L, 1L);
        assertNotNull(result);
        verify(shiftRepository, times(1)).save(shift);
    }

    @Test
    void testAssignEmployeeToShift_ShiftNotFound() {
        when(shiftRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> shiftService.assignEmployeeToShift(1L, 1L));
    }

    @Test
    void testAssignEmployeeToShift_EmployeeNotFound() {
        Shift shift = new Shift();
        shift.setId(1L);
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> shiftService.assignEmployeeToShift(1L, 1L));
    }

    @Test
    void testDeleteShift_Valid() {
        Shift shift = new Shift();
        shift.setId(1L);
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        shiftService.deleteShift(1L);
        verify(shiftRepository, times(1)).delete(shift);
    }
}