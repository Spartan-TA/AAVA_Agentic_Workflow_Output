package SpringBootTestSuite;

import com.example.warehouse.leave.LeaveRequest;
import com.example.warehouse.leave.LeaveService;
import com.example.warehouse.leave.LeaveRepository;
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
public class LeaveServiceTest {
    @Mock
    private LeaveRepository leaveRepository;

    @InjectMocks
    private LeaveService leaveService;

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
    public void requestLeave_ValidInput_ReturnsLeaveRequest() {
        LeaveRequest request = new LeaveRequest();
        request.setEmployeeId(1L);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(2));
        when(leaveRepository.save(any())).thenReturn(request);
        LeaveRequest result = leaveService.requestLeave(request);
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
    }

    @Test
    public void requestLeave_NullInput_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> leaveService.requestLeave(null));
    }

    @Test
    public void approveLeave_ValidId_ReturnsLeaveRequest() {
        LeaveRequest request = new LeaveRequest();
        request.setId(1L);
        request.setStatus("PENDING");
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(request));
        when(leaveRepository.save(any())).thenReturn(request);
        LeaveRequest result = leaveService.approveLeave(1L);
        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    public void approveLeave_InvalidId_ThrowsResourceNotFoundException() {
        when(leaveRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> leaveService.approveLeave(99L));
    }

    @Test
    public void denyLeave_ValidId_ReturnsLeaveRequest() {
        LeaveRequest request = new LeaveRequest();
        request.setId(1L);
        request.setStatus("PENDING");
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(request));
        when(leaveRepository.save(any())).thenReturn(request);
        LeaveRequest result = leaveService.denyLeave(1L);
        assertNotNull(result);
        assertEquals("DENIED", result.getStatus());
    }

    @Test
    public void denyLeave_InvalidId_ThrowsResourceNotFoundException() {
        when(leaveRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> leaveService.denyLeave(99L));
    }

    @Test
    public void getLeaveRequestsByEmployeeId_ValidId_ReturnsList() {
        LeaveRequest request = new LeaveRequest();
        request.setEmployeeId(1L);
        when(leaveRepository.findByEmployeeId(1L)).thenReturn(Collections.singletonList(request));
        List<LeaveRequest> result = leaveService.getLeaveRequestsByEmployeeId(1L);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void getLeaveRequestsByEmployeeId_NoRequests_ReturnsEmptyList() {
        when(leaveRepository.findByEmployeeId(1L)).thenReturn(Collections.emptyList());
        List<LeaveRequest> result = leaveService.getLeaveRequestsByEmployeeId(1L);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void requestLeave_InvalidDates_ThrowsValidationException() {
        LeaveRequest request = new LeaveRequest();
        request.setEmployeeId(1L);
        request.setStartDate(LocalDate.now().plusDays(2));
        request.setEndDate(LocalDate.now());
        assertThrows(ValidationException.class, () -> leaveService.requestLeave(request));
    }
}
