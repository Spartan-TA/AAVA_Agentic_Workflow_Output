package SpringBootTestSuite;

import com.example.dto.ShiftAssignmentDTO;
import com.example.entity.Schedule;
import com.example.repository.ScheduleRepository;
import com.example.service.ScheduleService;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void assignShift_ShouldSaveSchedule() {
        ShiftAssignmentDTO dto = new ShiftAssignmentDTO(1L, "Morning", LocalDate.now());
        Schedule schedule = new Schedule(1L, 1L, "Morning", LocalDate.now());

        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);

        Schedule result = scheduleService.assignShift(dto);

        assertNotNull(result);
        assertEquals("Morning", result.getShiftGroup());
    }

    @Test
    void hasConflict_ShouldReturnTrue_WhenConflictExists() {
        when(scheduleRepository.existsByEmployeeIdAndDate(1L, LocalDate.now())).thenReturn(true);

        boolean result = scheduleService.hasConflict(1L, LocalDate.now());

        assertTrue(result);
    }

    @Test
    void hasConflict_ShouldReturnFalse_WhenNoConflict() {
        when(scheduleRepository.existsByEmployeeIdAndDate(1L, LocalDate.now())).thenReturn(false);

        boolean result = scheduleService.hasConflict(1L, LocalDate.now());

        assertFalse(result);
    }

    @Test
    void getEmployeeSchedule_ShouldReturnSchedules() {
        List<Schedule> schedules = Arrays.asList(
            new Schedule(1L, 1L, "Morning", LocalDate.now())
        );
        when(scheduleRepository.findByEmployeeId(1L)).thenReturn(schedules);

        List<Schedule> result = scheduleService.getEmployeeSchedule(1L);

        assertEquals(1, result.size());
    }
}