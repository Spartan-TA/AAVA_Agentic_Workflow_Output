package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AttendanceControllerTest - Comprehensive unit tests for AttendanceController covering REST endpoints, security, geofence validation, boundaries, and edge cases.
 */
public class AttendanceControllerTest {
    private MockMvc mockMvc;
    private AttendanceController attendanceController;

    @BeforeEach
    public void setUp() {
        attendanceController = new AttendanceController();
        mockMvc = MockMvcBuilders.standaloneSetup(attendanceController).build();
    }

    @Test
    public void testClockIn201Created() throws Exception {
        String json = "{"employeeId":1,"timestamp":"2024-01-01T08:00:00"}";
        mockMvc.perform(post("/api/attendance/clock-in").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    @Test
    public void testClockIn400BadRequest() throws Exception {
        String json = "{"employeeId":null,"timestamp":""}";
        mockMvc.perform(post("/api/attendance/clock-in").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testClockOut200OK() throws Exception {
        String json = "{"employeeId":1,"timestamp":"2024-01-01T16:00:00"}";
        mockMvc.perform(post("/api/attendance/clock-out").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void testClockOut404NotFound() throws Exception {
        String json = "{"employeeId":9999,"timestamp":"2024-01-01T16:00:00"}";
        mockMvc.perform(post("/api/attendance/clock-out").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAttendanceByEmployee200OK() throws Exception {
        mockMvc.perform(get("/api/attendance/employee/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testCreateCorrection201Created() throws Exception {
        String json = "{"employeeId":1,"correctionType":"Missed Punch"}";
        mockMvc.perform(post("/api/attendance/corrections").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    @Test
    public void testSecurityRoleBasedAccess() throws Exception {
        mockMvc.perform(get("/api/attendance/employee/1").header("Authorization", "Bearer supervisor-token"))
                .andExpect(status().isOk());
    }

    @Test
    public void testSecurityUnauthorized() throws Exception {
        mockMvc.perform(get("/api/attendance/employee/1").header("Authorization", ""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGeofenceValidationValid() throws Exception {
        String json = "{"employeeId":1,"timestamp":"2024-01-01T08:00:00","geofenceValid":true}";
        mockMvc.perform(post("/api/attendance/clock-in").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    @Test
    public void testGeofenceValidationInvalid() throws Exception {
        String json = "{"employeeId":1,"timestamp":"2024-01-01T08:00:00","geofenceValid":false}";
        mockMvc.perform(post("/api/attendance/clock-in").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testBoundaryClockTimes() throws Exception {
        String json = "{"employeeId":1,"timestamp":"2024-01-01T00:00:00"}";
        mockMvc.perform(post("/api/attendance/clock-in").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    @Test
    public void testEdgeCaseNullInputs() throws Exception {
        String json = "{"employeeId":null,"timestamp":null}";
        mockMvc.perform(post("/api/attendance/clock-in").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }
}
