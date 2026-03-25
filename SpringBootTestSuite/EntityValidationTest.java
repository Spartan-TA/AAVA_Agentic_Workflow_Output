package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

// Assume these imports exist
import com.example.ems.dto.*;

public class EntityValidationTest {
    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testEmployeeDTO_Valid() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("John Doe");
        dto.setBadgeId("EMP001");
        dto.setRole("WORKER");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testEmployeeDTO_BlankName() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("");
        dto.setBadgeId("EMP001");
        dto.setRole("WORKER");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testAttendanceEventDTO_NullEmployeeId() {
        AttendanceEventDTO dto = new AttendanceEventDTO();
        dto.setEventType("CLOCK_IN");
        dto.setTimestamp(LocalDateTime.now());
        Set<ConstraintViolation<AttendanceEventDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testShiftTemplateDTO_BlankName() {
        ShiftTemplateDTO dto = new ShiftTemplateDTO();
        dto.setName("");
        dto.setStartTime(LocalDateTime.now());
        dto.setEndTime(LocalDateTime.now().plusHours(8));
        Set<ConstraintViolation<ShiftTemplateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testLeaveRequestDTO_MissingFields() {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        Set<ConstraintViolation<LeaveRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testCertificationDTO_Valid() {
        CertificationDTO dto = new CertificationDTO();
        dto.setEmployeeId(1L);
        dto.setName("Forklift License");
        dto.setIssueDate(LocalDate.now().minusYears(1));
        dto.setExpiryDate(LocalDate.now().plusYears(1));
        Set<ConstraintViolation<CertificationDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testSafetyIncidentDTO_BlankDescription() {
        SafetyIncidentDTO dto = new SafetyIncidentDTO();
        dto.setDescription("");
        dto.setSeverity("HIGH");
        dto.setReportedBy(1L);
        dto.setIncidentDate(LocalDate.now());
        Set<ConstraintViolation<SafetyIncidentDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testAssetDTO_BlankSerialNumber() {
        AssetDTO dto = new AssetDTO();
        dto.setAssetType("Forklift");
        dto.setSerialNumber("");
        Set<ConstraintViolation<AssetDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testAssetAssignmentDTO_NullAssetId() {
        AssetAssignmentDTO dto = new AssetAssignmentDTO();
        dto.setEmployeeId(1L);
        dto.setCheckoutTime(LocalDateTime.now());
        Set<ConstraintViolation<AssetAssignmentDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testPerformanceReviewDTO_NullReviewDate() {
        PerformanceReviewDTO dto = new PerformanceReviewDTO();
        dto.setEmployeeId(1L);
        Set<ConstraintViolation<PerformanceReviewDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}
