import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.Optional;
import javax.validation.ValidationException;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
public class LeaveServiceTest {
    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private LeaveServiceImpl leaveService;

    private LeaveRequestDto validLeaveRequestDto;
    private LeaveRequest validLeaveRequest;
    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setName("John Doe");
        validEmployee.setBadgeId("EMP001");
        validEmployee.setLeaveBalance(10);

        validLeaveRequestDto = new LeaveRequestDto();
        validLeaveRequestDto.setEmployeeId(1L);
        validLeaveRequestDto.setStartDate(LocalDate.now().plusDays(1));
        validLeaveRequestDto.setEndDate(LocalDate.now().plusDays(3));
        validLeaveRequestDto.setReason("Vacation");

        validLeaveRequest = new LeaveRequest();
        validLeaveRequest.setId(1L);
        validLeaveRequest.setEmployee(validEmployee);
        validLeaveRequest.setStartDate(validLeaveRequestDto.getStartDate());
        validLeaveRequest.setEndDate(validLeaveRequestDto.getEndDate());
        validLeaveRequest.setStatus(LeaveStatus.PENDING);
    }

    @Test
    void testRequestLeave_ValidInput() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(validLeaveRequest);
        LeaveRequest result = leaveService.requestLeave(validLeaveRequestDto);
        assertNotNull(result);
        assertEquals(LeaveStatus.PENDING, result.getStatus());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    void testRequestLeave_InvalidDateRange() {
        validLeaveRequestDto.setEndDate(LocalDate.now().minusDays(1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        assertThrows(ValidationException.class, () -> leaveService.requestLeave(validLeaveRequestDto));
    }

    @Test
    void testRequestLeave_NullEmployee() {
        validLeaveRequestDto.setEmployeeId(null);
        assertThrows(ValidationException.class, () -> leaveService.requestLeave(validLeaveRequestDto));
    }

    @Test
    void testRequestLeave_InsufficientBalance() {
        validEmployee.setLeaveBalance(0);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        assertThrows(ValidationException.class, () -> leaveService.requestLeave(validLeaveRequestDto));
    }

    @Test
    void testApproveLeave_ValidId() {
        validLeaveRequest.setStatus(LeaveStatus.PENDING);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(validLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(validLeaveRequest);
        LeaveRequest result = leaveService.approveLeave(1L);
        assertEquals(LeaveStatus.APPROVED, result.getStatus());
    }

    @Test
    void testApproveLeave_AlreadyApproved() {
        validLeaveRequest.setStatus(LeaveStatus.APPROVED);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(validLeaveRequest));
        LeaveRequest result = leaveService.approveLeave(1L);
        assertEquals(LeaveStatus.APPROVED, result.getStatus());
    }

    @Test
    void testApproveLeave_NonExistentId() {
        when(leaveRequestRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> leaveService.approveLeave(2L));
    }

    @Test
    void testDenyLeave_ValidId() {
        validLeaveRequest.setStatus(LeaveStatus.PENDING);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(validLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(validLeaveRequest);
        LeaveRequest result = leaveService.denyLeave(1L);
        assertEquals(LeaveStatus.DENIED, result.getStatus());
    }

    @Test
    void testDenyLeave_NonExistentId() {
        when(leaveRequestRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> leaveService.denyLeave(2L));
    }

    @Test
    void testGetBalance_ValidEmployeeId() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        int balance = leaveService.getBalance(1L);
        assertEquals(10, balance);
    }

    @Test
    void testGetBalance_NonExistentEmployee() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> leaveService.getBalance(2L));
    }
}