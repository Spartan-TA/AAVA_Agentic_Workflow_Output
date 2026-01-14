package com.example.warehouse.test;

import com.example.warehouse.schedule.Schedule;
import com.example.warehouse.schedule.ScheduleRepository;
import com.example.warehouse.schedule.ScheduleService;
import com.example.warehouse.schedule.ScheduleController;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScheduleServiceTest {
    @Mock
    private ScheduleRepository scheduleRepository;
    @InjectMocks
    private ScheduleService scheduleService;
    private ScheduleController scheduleController;
    private Schedule testSchedule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduleController = new ScheduleController(scheduleService);
        testSchedule = new Schedule(1L, "Morning Shift", LocalDate.now(), LocalDate.now().plusDays(1), 1L, false);
    }

    @AfterEach
    void tearDown() {
        // Cleanup if needed
    }

    @Test
    void testCreateSchedule_ValidInput_Success() {
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(testSchedule);
        Schedule created = scheduleService.createSchedule(testSchedule);
        assertNotNull(created);
        assertEquals("Morning Shift", created.getName());
    }

    @Test
    void testCreateSchedule_Conflict_ThrowsException() {
        when(scheduleRepository.hasConflict(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> scheduleService.createSchedule(testSchedule));
    }

    @Test
    void testGetScheduleById_ValidId_ReturnsSchedule() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        Schedule found = scheduleService.getScheduleById(1L);
        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void testGetScheduleById_InvalidId_ThrowsException() {
        when(scheduleRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> scheduleService.getScheduleById(2L));
    }

    @Test
    void testListSchedules_EmptyList() {
        when(scheduleRepository.findAll()).thenReturn(Collections.emptyList());
        List<Schedule> result = scheduleService.listSchedules();
        assertTrue(result.isEmpty());
    }

    @Test
    void testController_CreateSchedule_Success() {
        when(scheduleService.createSchedule(any(Schedule.class))).thenReturn(testSchedule);
        ResponseEntity<Schedule> response = scheduleController.createSchedule(testSchedule);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Morning Shift", response.getBody().getName());
    }

    @Test
    void testController_CreateSchedule_Conflict() {
        when(scheduleService.createSchedule(any(Schedule.class))).thenThrow(new IllegalStateException("Conflict"));
        assertThrows(IllegalStateException.class, () -> scheduleController.createSchedule(testSchedule));
    }
}
