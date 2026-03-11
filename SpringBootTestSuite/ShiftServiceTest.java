package SpringBootTestSuite;

import com.example.ems.entity.Shift;
import com.example.ems.service.ShiftService;
import com.example.ems.repository.ShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ShiftServiceTest {

    @Autowired
    private ShiftService shiftService;

    @MockBean
    private ShiftRepository shiftRepository;

    private Shift shift;

    @BeforeEach
    void setUp() {
        shift = new Shift();
        shift.setId(1L);
        shift.setName("Morning Shift");
        shift.setStartTime(LocalTime.of(8, 0));
        shift.setEndTime(LocalTime.of(16, 0));
        shift.setDaysOfWeek("MON,TUE,WED,THU,FRI");
    }

    @Test
    void testGetAllShiftsReturnsList() {
        Mockito.when(shiftRepository.findAll()).thenReturn(Arrays.asList(shift));
        assertEquals(1, shiftService.getAllShifts().size());
    }

    @Test
    void testGetShiftByIdSuccess() {
        Mockito.when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        Shift found = shiftService.getShiftById(1L);
        assertEquals("Morning Shift", found.getName());
    }

    @Test
    void testGetShiftByIdNotFoundThrowsException() {
        Mockito.when(shiftRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> shiftService.getShiftById(2L));
    }

    @Test
    void testCreateShiftSuccess() {
        Mockito.when(shiftRepository.save(shift)).thenReturn(shift);
        Shift created = shiftService.createShift(shift);
        assertEquals("Morning Shift", created.getName());
    }

    @Test
    void testCreateShiftNullFieldsThrowsException() {
        Shift invalid = new Shift();
        assertThrows(RuntimeException.class, () -> shiftService.createShift(invalid));
    }

    @Test
    void testUpdateShiftSuccess() {
        Mockito.when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        shift.setName("Evening Shift");
        Mockito.when(shiftRepository.save(shift)).thenReturn(shift);
        Shift updated = shiftService.updateShift(1L, shift);
        assertEquals("Evening Shift", updated.getName());
    }

    @Test
    void testUpdateShiftNotFoundThrowsException() {
        Mockito.when(shiftRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> shiftService.updateShift(2L, shift));
    }

    @Test
    void testDeleteShiftSuccess() {
        Mockito.when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        assertDoesNotThrow(() -> shiftService.deleteShift(1L));
    }

    @Test
    void testDeleteShiftNotFoundThrowsException() {
        Mockito.when(shiftRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> shiftService.deleteShift(2L));
    }

    @Test
    void testCreateShiftInvalidTimeThrowsException() {
        Shift invalid = new Shift();
        invalid.setStartTime(LocalTime.of(16, 0));
        invalid.setEndTime(LocalTime.of(8, 0));
        assertThrows(RuntimeException.class, () -> shiftService.createShift(invalid));
    }
}
