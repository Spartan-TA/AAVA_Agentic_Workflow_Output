package com.warehouseems.common;

import com.warehouseems.employee.EmployeeController;
import com.warehouseems.employee.EmployeeService;
import com.warehouseems.employee.dto.EmployeeRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test suite for GlobalExceptionHandler.
 * Tests all exception handling scenarios including validation errors, access denied, and general exceptions.
 */
@WebMvcTest(controllers = {EmployeeController.class, GlobalExceptionHandler.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeRequestDto validRequest;
    private EmployeeRequestDto invalidRequest;

    @BeforeEach
    void setUp() {
        validRequest = new EmployeeRequestDto();
        validRequest.setName("John Doe");
        validRequest.setBadgeId("EMP001");
        validRequest.setRole("WORKER");
        validRequest.setDepartment("Shipping");
        validRequest.setShiftGroup("DAY_SHIFT");
        validRequest.setHireDate(LocalDate.of(2023, 1, 1));
        validRequest.setStatus("ACTIVE");
        validRequest.setEmail("john.doe@warehouse.com");
        validRequest.setPhone("+1234567890");

        invalidRequest = new EmployeeRequestDto();
    }

    // ==================== VALIDATION ERROR TESTS ====================

    @Nested
    @DisplayName("Validation Error Tests")
    class ValidationErrorTests {

        @Test
        @DisplayName("Should handle missing name validation error")
        @WithMockUser(roles = "ADMIN")
        void testValidationError_MissingName() throws Exception {
            invalidRequest.setBadgeId("EMP001");
            invalidRequest.setRole("WORKER");
            invalidRequest.setDepartment("Shipping");
            invalidRequest.setHireDate(LocalDate.now());
            invalidRequest.setStatus("ACTIVE");

            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"badgeId":"EMP001","role":"WORKER","department":"Shipping","hireDate":"2023-01-01","status":"ACTIVE"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.errors.name").exists());
        }

        @Test
        @DisplayName("Should handle empty name validation error")
        @WithMockUser(roles = "ADMIN")
        void testValidationError_EmptyName() throws Exception {
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"name":"","badgeId":"EMP001","role":"WORKER","department":"Shipping","hireDate":"2023-01-01","status":"ACTIVE"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }

        @Test
        @DisplayName("Should handle missing badge ID validation error")
        @WithMockUser(roles = "ADMIN")
        void testValidationError_MissingBadgeId() throws Exception {
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"name":"John Doe","role":"WORKER","department":"Shipping","hireDate":"2023-01-01","status":"ACTIVE"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.badgeId").exists());
        }

        @Test
        @DisplayName("Should handle invalid badge ID format validation error")
        @WithMockUser(roles = "ADMIN")
        void testValidationError_InvalidBadgeIdFormat() throws Exception {
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"name":"John Doe","badgeId":"emp","role":"WORKER","department":"Shipping","hireDate":"2023-01-01","status":"ACTIVE"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.badgeId").exists());
        }

        @Test
        @DisplayName("Should handle invalid email validation error")
        @WithMockUser(roles = "ADMIN")
        void testValidationError_InvalidEmail() throws Exception {
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"name":"John Doe","badgeId":"EMP001","role":"WORKER","department":"Shipping","hireDate":"2023-01-01","status":"ACTIVE","email":"invalid-email"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.email").exists());
        }

        @Test
        @DisplayName("Should handle invalid phone validation error")
        @WithMockUser(roles = "ADMIN")
        void testValidationError_InvalidPhone() throws Exception {
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"name":"John Doe","badgeId":"EMP001","role":"WORKER","department":"Shipping","hireDate":"2023-01-01","status":"ACTIVE","phone":"invalid"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.phone").exists());
        }

        @Test
        @DisplayName("Should handle multiple validation errors")
        @WithMockUser(roles = "ADMIN")
        void testValidationError_MultipleErrors() throws Exception {
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"email":"invalid-email","phone":"invalid"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.errors").isMap());
        }

        @Test
        @DisplayName("Should include timestamp in validation error response")
        @WithMockUser(roles = "ADMIN")
        void testValidationError_IncludesTimestamp() throws Exception {
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    // ==================== ILLEGAL ARGUMENT EXCEPTION TESTS ====================

    @Nested
    @DisplayName("Illegal Argument Exception Tests")
    class IllegalArgumentExceptionTests {

        @Test
        @DisplayName("Should handle duplicate badge ID exception")
        @WithMockUser(roles = "ADMIN")
        void testIllegalArgumentException_DuplicateBadgeId() throws Exception {
            when(employeeService.createEmployee(any(EmployeeRequestDto.class)))
                    .thenThrow(new IllegalArgumentException("Badge ID already exists: EMP001"));

            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"name":"John Doe","badgeId":"EMP001","role":"WORKER","department":"Shipping","hireDate":"2023-01-01","status":"ACTIVE"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Badge ID already exists: EMP001"));
        }

        @Test
        @DisplayName("Should handle generic illegal argument exception")
        @WithMockUser(roles = "ADMIN")
        void testIllegalArgumentException_Generic() throws Exception {
            when(employeeService.createEmployee(any(EmployeeRequestDto.class)))
                    .thenThrow(new IllegalArgumentException("Invalid input provided"));

            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"name":"John Doe","badgeId":"EMP001","role":"WORKER","department":"Shipping","hireDate":"2023-01-01","status":"ACTIVE"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Invalid input provided"));
        }

        @Test
        @DisplayName("Should include timestamp in illegal argument error response")
        @WithMockUser(roles = "ADMIN")
        void testIllegalArgumentException_IncludesTimestamp() throws Exception {
            when(employeeService.createEmployee(any(EmployeeRequestDto.class)))
                    .thenThrow(new IllegalArgumentException("Test error"));

            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"name":"John Doe","badgeId":"EMP001","role":"WORKER","department":"Shipping","hireDate":"2023-01-01","status":"ACTIVE"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    // ==================== ACCESS DENIED EXCEPTION TESTS ====================

    @Nested
    @DisplayName("Access Denied Exception Tests")
    class AccessDeniedExceptionTests {

        @Test
        @DisplayName("Should handle access denied for WORKER role")
        @WithMockUser(roles = "WORKER")
        void testAccessDeniedException_WorkerRole() throws Exception {
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"name":"John Doe","badgeId":"EMP001","role":"WORKER","department":"Shipping","hireDate":"2023-01-01","status":"ACTIVE"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should handle access denied for SUPERVISOR role")
        @WithMockUser(roles = "SUPERVISOR")
        void testAccessDeniedException_SupervisorRole() throws Exception {
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"name":"John Doe","badgeId":"EMP001","role":"WORKER","department":"Shipping","hireDate":"2023-01-01","status":"ACTIVE"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return proper error message for access denied")
        @WithMockUser(roles = "WORKER")
        void testAccessDeniedException_ErrorMessage() throws Exception {
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"name":"John Doe","badgeId":"EMP001","role":"WORKER","department":"Shipping","hireDate":"2023-01-01","status":"ACTIVE"}"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GENERAL EXCEPTION TESTS ====================

    @Nested
    @DisplayName("General Exception Tests")
    class GeneralExceptionTests {

        @Test
        @DisplayName("Should handle runtime exception")
        @WithMockUser(roles = "ADMIN")
        void testGeneralException_RuntimeException() throws Exception {
            when(employeeService.createEmployee(any(EmployeeRequestDto.class)))
                    .thenThrow(new RuntimeException("Unexpected error occurred"));

            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"name":"John Doe","badgeId":"EMP001","role":"WORKER","department":"Shipping","hireDate":"2023-01-01","status":"ACTIVE"}"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
        }

        @Test
        @DisplayName("Should handle null pointer exception")
        @WithMockUser(roles = "ADMIN")
        void testGeneralException_NullPointerException() throws Exception {
            when(employeeService.createEmployee(any(EmployeeRequestDto.class)))
                    .thenThrow(new NullPointerException("Null value encountered"));

            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"name":"John Doe","badgeId":"EMP001","role":"WORKER","department":"Shipping","hireDate":"2023-01-01","status":"ACTIVE"}"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500));
        }

        @Test
        @DisplayName("Should include timestamp in general error response")
        @WithMockUser(roles = "ADMIN")
        void testGeneralException_IncludesTimestamp() throws Exception {
            when(employeeService.createEmployee(any(EmployeeRequestDto.class)))
                    .thenThrow(new RuntimeException("Test error"));

            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"name":"John Doe","badgeId":"EMP001","role":"WORKER","department":"Shipping","hireDate":"2023-01-01","status":"ACTIVE"}"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should not expose internal error details")
        @WithMockUser(roles = "ADMIN")
        void testGeneralException_NoInternalDetails() throws Exception {
            when(employeeService.createEmployee(any(EmployeeRequestDto.class)))
                    .thenThrow(new RuntimeException("Database connection failed: password=secret123"));

            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"name":"John Doe","badgeId":"EMP001","role":"WORKER","department":"Shipping","hireDate":"2023-01-01","status":"ACTIVE"}"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
        }
    }

    // ==================== EDGE CASE TESTS ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle malformed JSON")
        @WithMockUser(roles = "ADMIN")
        void testEdgeCase_MalformedJson() throws Exception {
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle empty request body")
        @WithMockUser(roles = "ADMIN")
        void testEdgeCase_EmptyRequestBody() throws Exception {
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle null request body")
        @WithMockUser(roles = "ADMIN")
        void testEdgeCase_NullRequestBody() throws Exception {
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle very large request body")
        @WithMockUser(roles = "ADMIN")
        void testEdgeCase_LargeRequestBody() throws Exception {
            String largeAddress = "A".repeat(10000);
            String requestBody = String.format(
                    "{"name":"John Doe","badgeId":"EMP001","role":"WORKER","department":"Shipping","hireDate":"2023-01-01","status":"ACTIVE","address":"%s"}",
                    largeAddress
            );

            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle special characters in error messages")
        @WithMockUser(roles = "ADMIN")
        void testEdgeCase_SpecialCharactersInError() throws Exception {
            when(employeeService.createEmployee(any(EmployeeRequestDto.class)))
                    .thenThrow(new IllegalArgumentException("Error with special chars: <>&"'"));

            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"name":"John Doe","badgeId":"EMP001","role":"WORKER","department":"Shipping","hireDate":"2023-01-01","status":"ACTIVE"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
        }
    }
}