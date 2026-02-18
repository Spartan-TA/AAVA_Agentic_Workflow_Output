package SpringBootTestSuite;

import com.example.controller.ShiftController;
import com.example.dto.ShiftAssignmentDTO;
import com.example.entity.Schedule;
import com.example.service.ScheduleService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShiftControllerTest {

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private ShiftController shiftController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void assignShift_ShouldReturnSchedule() {
        ShiftAssignmentDTO dto = new ShiftAssignmentDTO(1L, "Morning", LocalDate.now());
        Schedule schedule = new Schedule(1L, 1L, "Morning", LocalDate.now());

        when(scheduleService.assignShift(dto)).thenReturn(schedule);

        ResponseEntity<Schedule> response = shiftController.assignShift(dto);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Morning", response.getBody().getShiftGroup());
    }

    @Test
    void getEmployeeSchedule_ShouldReturnSchedules() {
        List<Schedule> schedules = Arrays.asList(
            new Schedule(1L, 1L, "Morning", LocalDate.now())
        );
        when(scheduleService.getEmployeeSchedule(1L)).thenReturn(schedules);

        ResponseEntity<List<Schedule>> response = shiftController.getEmployeeSchedule(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }
}