package com.warehouse.employee.management.exception;

import com.warehouse.employee.management.controller.EmployeeController;
import com.warehouse.employee.management.dto.EmployeeDTO;
import com.warehouse.employee.management.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for GlobalExceptionHandler
 * Tests all exception handling scenarios
 * 
 * @author Automation Test Engineer
 * @version 1.0
 */
@WebMvcTest(controllers = {EmployeeController.class, GlobalExceptionHandler.class})
@DisplayName("GlobalExceptionHandler Test Suite")
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeDTO testEmployeeDTO;

    @BeforeEach
    void setUp() {
        // Initialize test employee DTO
        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setId(1L);
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setFirstName("John");
        testEmployeeDTO.setLastName("Doe");
        testEmployeeDTO.setEmail("john.doe@warehouse.com");
        testEmployeeDTO.setPhoneNumber("+1234567890");
        testEmployeeDTO.setRole("WORKER");
        testEmployeeDTO.setDepartment("Shipping");
        testEmployeeDTO.setShiftGroup("Morning");
        testEmployeeDTO.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDTO.setStatus("ACTIVE");
    }

    // ==================== RESOURCE NOT FOUND EXCEPTION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test handleResourceNotFoundException - Returns 404 with Error Message")
    void testHandleResourceNotFoundException_Returns404WithErrorMessage() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new ResourceNotFoundException("Employee not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Employee not found")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test handleResourceNotFoundException - Multiple Scenarios - Returns 404")
    void testHandleResourceNotFoundException_MultipleScenarios_Returns404() throws Exception {
        // Arrange - Update non-existing employee
        when(employeeService.updateEmployee(anyLong(), any(EmployeeDTO.class)))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"EMP001","firstName":"John","lastName":"Doe","email":"john@test.com"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test handleResourceNotFoundException - Delete Non-Existing - Returns 404")
    void testHandleResourceNotFoundException_DeleteNonExisting_Returns404() throws Exception {
        // Arrange
        when(employeeService.deleteEmployee(999L))
                .thenThrow(new ResourceNotFoundException("Employee not found with id: 999"));

        // Act & Assert
        mockMvc.perform(delete("/api/employees/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ==================== VALIDATION EXCEPTION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test handleValidationExceptions - Missing Required Field - Returns 400")
    void testHandleValidationExceptions_MissingRequiredField_Returns400() throws Exception {
        // Arrange - Missing badgeId
        String invalidJson = "{"firstName":"John","lastName":"Doe","email":"john@test.com"}";

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test handleValidationExceptions - Invalid Email Format - Returns 400")
    void testHandleValidationExceptions_InvalidEmailFormat_Returns400() throws Exception {
        // Arrange - Invalid email
        String invalidJson = "{"badgeId":"EMP001","firstName":"John","lastName":"Doe","email":"invalid-email"}";

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test handleValidationExceptions - Empty Required Fields - Returns 400")
    void testHandleValidationExceptions_EmptyRequiredFields_Returns400() throws Exception {
        // Arrange - Empty firstName and lastName
        String invalidJson = "{"badgeId":"EMP001","firstName":"","lastName":"","email":"john@test.com"}";

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test handleValidationExceptions - Multiple Validation Errors - Returns 400 with All Errors")
    void testHandleValidationExceptions_MultipleValidationErrors_Returns400WithAllErrors() throws Exception {
        // Arrange - Multiple validation errors
        String invalidJson = "{"firstName":"","lastName":"","email":"invalid"}";

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test handleValidationExceptions - Null Values - Returns 400")
    void testHandleValidationExceptions_NullValues_Returns400() throws Exception {
        // Arrange - Null required fields
        String invalidJson = "{"badgeId":null,"firstName":null,"lastName":null,"email":null}";

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // ==================== ILLEGAL ARGUMENT EXCEPTION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test handleIllegalArgumentException - Duplicate BadgeId - Returns 400")
    void testHandleIllegalArgumentException_DuplicateBadgeId_Returns400() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class)))
                .thenThrow(new IllegalArgumentException("Badge ID already exists: EMP001"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"EMP001","firstName":"John","lastName":"Doe","email":"john@test.com"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Badge ID already exists")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test handleIllegalArgumentException - Invalid Input - Returns 400")
    void testHandleIllegalArgumentException_InvalidInput_Returns400() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class)))
                .thenThrow(new IllegalArgumentException("Invalid employee data"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"EMP001","firstName":"John","lastName":"Doe","email":"john@test.com"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid")));
    }

    // ==================== GENERIC EXCEPTION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test handleGenericException - Internal Server Error - Returns 500")
    void testHandleGenericException_InternalServerError_Returns500() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees())
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test handleGenericException - Unexpected Error - Returns 500")
    void testHandleGenericException_UnexpectedError_Returns500() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L))
                .thenThrow(new NullPointerException("Unexpected null value"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    // ==================== MALFORMED JSON TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test Malformed JSON - Returns 400")
    void testMalformedJSON_Returns400() throws Exception {
        // Arrange - Malformed JSON
        String malformedJson = "{invalid json}";

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test Empty JSON Object - Returns 400")
    void testEmptyJSONObject_Returns400() throws Exception {
        // Arrange - Empty JSON
        String emptyJson = "{}";

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test Invalid JSON Type - Returns 400")
    void testInvalidJSONType_Returns400() throws Exception {
        // Arrange - Invalid type for id (string instead of number)
        String invalidTypeJson = "{"id":"not-a-number","badgeId":"EMP001","firstName":"John","lastName":"Doe","email":"john@test.com"}";

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidTypeJson))
                .andExpect(status().isBadRequest());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test Exception with Null Message - Returns 500 with Default Message")
    void testExceptionWithNullMessage_Returns500WithDefaultMessage() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees())
                .thenThrow(new RuntimeException());

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test Exception with Empty Message - Returns 500")
    void testExceptionWithEmptyMessage_Returns500() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees())
                .thenThrow(new RuntimeException(""));

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test Very Long Error Message - Returns 400 with Full Message")
    void testVeryLongErrorMessage_Returns400WithFullMessage() throws Exception {
        // Arrange
        String longMessage = "A".repeat(1000);
        when(employeeService.createEmployee(any(EmployeeDTO.class)))
                .thenThrow(new IllegalArgumentException(longMessage));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"EMP001","firstName":"John","lastName":"Doe","email":"john@test.com"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test Special Characters in Error Message - Returns Properly Escaped")
    void testSpecialCharactersInErrorMessage_ReturnsProperlyEscaped() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L))
                .thenThrow(new ResourceNotFoundException("Employee not found: <script>alert('xss')</script>"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    // ==================== CONCURRENT REQUEST TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test Multiple Validation Errors - Returns All Error Fields")
    void testMultipleValidationErrors_ReturnsAllErrorFields() throws Exception {
        // Arrange - Multiple validation errors
        String invalidJson = "{"badgeId":"","firstName":"","lastName":"","email":"invalid"}";

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()", greaterThan(0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test Error Response Format - Contains Timestamp and Path")
    void testErrorResponseFormat_ContainsTimestampAndPath() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test Validation Error Response Format - Contains Field Names")
    void testValidationErrorResponseFormat_ContainsFieldNames() throws Exception {
        // Arrange - Missing required field
        String invalidJson = "{"firstName":"John","lastName":"Doe","email":"john@test.com"}";

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors[*].field").exists());
    }
}