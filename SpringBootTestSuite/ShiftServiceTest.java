package SpringBootTestSuite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShiftServiceTest {

    @Mock
    private ShiftAssignmentRepository assignmentRepository;
    @Mock
    private ShiftTemplateRepository templateRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ShiftService shiftService;

    private ShiftAssignmentRequest request;
    private Employee employee;
    private ShiftTemplate template;
    private ShiftAssignment assignment;
    private ShiftAssignmentResponse response;

    @BeforeEach
    void setUp() {
        request = new ShiftAssignmentRequest();
        request.setEmployeeId(1L);
        request.setTemplateId(2L);
        request.setShiftDate(LocalDate.of(2023, 1, 1));

        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("BADGE123");

        template = new ShiftTemplate();
        template.setId(2L);
        template.setStartTime("08:00");
        template.setEndTime("16:00");

        assignment = ShiftAssignment.builder()
                .id(100L)
                .employee(employee)
                .template(template)
                .shiftDate(request.getShiftDate())
                .status(ShiftAssignment.ShiftStatus.SCHEDULED)
                .build();

        response = new ShiftAssignmentResponse();
        response.setId(100L);
    }

    @Test
    void testAssignShift_Normal_Success() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(templateRepository.findById(anyLong())).thenReturn(Optional.of(template));
        when(assignmentRepository.findByEmployeeAndShiftDate(any(Employee.class), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(assignmentRepository.save(any(ShiftAssignment.class))).thenReturn(assignment);
        Mockito.doReturn(response).when(shiftService).mapToResponse(any(ShiftAssignment.class));

        ShiftAssignmentResponse resp = shiftService.assignShift(request);
        assertNotNull(resp);
        assertEquals(100L, resp.getId());
    }

    @Test
    void testAssignShift_EmployeeNotFound_ThrowsException() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> shiftService.assignShift(request));
    }

    @Test
    void testAssignShift_TemplateNotFound_ThrowsException() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(templateRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> shiftService.assignShift(request));
    }

    @Test
    void testAssignShift_HasConflict_ThrowsException() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(templateRepository.findById(anyLong())).thenReturn(Optional.of(template));
        ShiftAssignment conflictingAssignment = ShiftAssignment.builder()
                .employee(employee)
                .template(template)
                .shiftDate(request.getShiftDate())
                .status(ShiftAssignment.ShiftStatus.SCHEDULED)
                .build();
        when(assignmentRepository.findByEmployeeAndShiftDate(any(Employee.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(conflictingAssignment));
        Mockito.doReturn(true).when(shiftService).shiftsOverlap(any(ShiftTemplate.class), any(ShiftTemplate.class));
        assertThrows(ShiftConflictException.class, () -> shiftService.assignShift(request));
    }

    @Test
    void testHasConflict_NoAssignments_ReturnsFalse() {
        when(assignmentRepository.findByEmployeeAndShiftDate(any(Employee.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        boolean conflict = shiftService.hasConflict(employee, LocalDate.now(), template);
        assertFalse(conflict);
    }

    @Test
    void testHasConflict_Overlap_ReturnsTrue() {
        ShiftTemplate otherTemplate = new ShiftTemplate();
        otherTemplate.setId(3L);
        otherTemplate.setStartTime("08:00");
        otherTemplate.setEndTime("12:00");
        ShiftAssignment existing = ShiftAssignment.builder()
                .employee(employee)
                .template(otherTemplate)
                .shiftDate(LocalDate.now())
                .status(ShiftAssignment.ShiftStatus.SCHEDULED)
                .build();
        when(assignmentRepository.findByEmployeeAndShiftDate(any(Employee.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(existing));
        Mockito.doReturn(true).when(shiftService).shiftsOverlap(any(ShiftTemplate.class), any(ShiftTemplate.class));
        boolean conflict = shiftService.hasConflict(employee, LocalDate.now(), template);
        assertTrue(conflict);
    }

    @Test
    void testHasConflict_NoOverlap_ReturnsFalse() {
        ShiftTemplate otherTemplate = new ShiftTemplate();
        otherTemplate.setId(3L);
        otherTemplate.setStartTime("18:00");
        otherTemplate.setEndTime("22:00");
        ShiftAssignment existing = ShiftAssignment.builder()
                .employee(employee)
                .template(otherTemplate)
                .shiftDate(LocalDate.now())
                .status(ShiftAssignment.ShiftStatus.SCHEDULED)
                .build();
        when(assignmentRepository.findByEmployeeAndShiftDate(any(Employee.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(existing));
        Mockito.doReturn(false).when(shiftService).shiftsOverlap(any(ShiftTemplate.class), any(ShiftTemplate.class));
        boolean conflict = shiftService.hasConflict(employee, LocalDate.now(), template);
        assertFalse(conflict);
    }

    @Test
    void testAssignShift_SaveThrowsException_ThrowsException() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(templateRepository.findById(anyLong())).thenReturn(Optional.of(template));
        when(assignmentRepository.findByEmployeeAndShiftDate(any(Employee.class), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(assignmentRepository.save(any(ShiftAssignment.class))).thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class, () -> shiftService.assignShift(request));
    }
}
