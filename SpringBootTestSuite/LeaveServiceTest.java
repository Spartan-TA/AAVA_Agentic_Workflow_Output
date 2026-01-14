package com.example.warehouse.test;

import com.example.warehouse.leave.Leave;
import com.example.warehouse.leave.LeaveRepository;
import com.example.warehouse.leave.LeaveService;
import com.example.warehouse.leave.LeaveController;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeaveServiceTest {
    @Mock
    private LeaveRepository leaveRepository;
    @InjectMocks
    private LeaveService leaveService;
    private LeaveController leaveController;
    private Leave testLeave;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        leaveController = new LeaveController(leaveService);
        testLeave = new Leave(1L, 1L, "PTO", LocalDate.now(), LocalDate.now().plusDays(2), "PENDING", 16);
    }

    @AfterEach
    void tearDown() {
        // Cleanup if needed
    }

    @Test
    void testRequestLeave_ValidInput_Success() {
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);
        Leave created = leaveService.requestLeave(testLeave);
        assertNotNull(created);
        assertEquals("PTO", created.getType());
    }

    @Test
    void testRequestLeave_InsufficientBalance_ThrowsException() {
        Leave insufficient = new Leave(1L, 1L, "PTO", LocalDate.now(), LocalDate.now().plusDays(10), "PENDING", 8);
        when(leaveService.hasSufficientBalance(anyLong(), anyString(), anyInt())).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> leaveService.requestLeave(insufficient));
    }

    @Test
    void testApproveLeave_Valid_Success() {
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);
        Leave approved = leaveService.approveLeave(1L);
        assertEquals("APPROVED", approved.getStatus());
    }

    @Test
    void testApproveLeave_InvalidId_ThrowsException() {
        when(leaveRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> leaveService.approveLeave(2L));
    }

    @Test
    void testGetLeaveByEmployeeId_EmptyList() {
        when(leaveRepository.findByEmployeeId(2L)).thenReturn(Collections.emptyList());
        List<Leave> result = leaveService.getLeaveByEmployeeId(2L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testController_RequestLeave_Success() {
        when(leaveService.requestLeave(any(Leave.class))).thenReturn(testLeave);
        ResponseEntity<Leave> response = leaveController.requestLeave(testLeave);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals("PTO", response.getBody().getType());
    }

    @Test
    void testController_ApproveLeave_Success() {
        when(leaveService.approveLeave(anyLong())).thenReturn(testLeave);
        ResponseEntity<Leave> response = leaveController.approveLeave(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("PTO", response.getBody().getType());
    }

    @Test
    void testController_ApproveLeave_NotFound() {
        when(leaveService.approveLeave(anyLong())).thenThrow(new NoSuchElementException("Not found"));
        assertThrows(NoSuchElementException.class, () -> leaveController.approveLeave(2L));
    }
}
