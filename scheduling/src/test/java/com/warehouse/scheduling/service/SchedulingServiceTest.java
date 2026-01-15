package com.warehouse.scheduling.service;

import com.warehouse.scheduling.entity.Schedule;
import com.warehouse.scheduling.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchedulingServiceTest {
    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private SchedulingService schedulingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetScheduleById() {
        Schedule sched = Schedule.builder().id(1L).date(LocalDate.now()).shift("Morning").build();
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(sched));
        Optional<Schedule> result = schedulingService.getScheduleById(1L);
        assertTrue(result.isPresent());
        assertEquals("Morning", result.get().getShift());
    }
}
