package SpringBootTestSuite;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.controller.AttendanceController;
import com.example.service.AttendanceService;

@WebMvcTest(AttendanceController.class)
public class AttendanceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    @Test
    @WithMockUser
    void testClockIn_ValidRequest_Returns201() throws Exception {
        doNothing().when(attendanceService).clockIn(anyLong());
        mockMvc.perform(post("/attendance/clock-in")
                .contentType("application/json")
                .content("{"employeeId":1}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void testClockIn_AlreadyClockedIn_Returns400() throws Exception {
        doThrow(new IllegalStateException("Already clocked in")).when(attendanceService).clockIn(anyLong());
        mockMvc.perform(post("/attendance/clock-in")
                .contentType("application/json")
                .content("{"employeeId":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testClockIn_Unauthorized_Returns401() throws Exception {
        mockMvc.perform(post("/attendance/clock-in")
                .contentType("application/json")
                .content("{"employeeId":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testClockOut_ValidRequest_Returns200() throws Exception {
        doNothing().when(attendanceService).clockOut(anyLong());
        mockMvc.perform(post("/attendance/clock-out")
                .contentType("application/json")
                .content("{"employeeId":1}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testClockOut_NotClockedIn_Returns400() throws Exception {
        doThrow(new IllegalStateException("Not clocked in")).when(attendanceService).clockOut(anyLong());
        mockMvc.perform(post("/attendance/clock-out")
                .contentType("application/json")
                .content("{"employeeId":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testGetDailyHours_ValidRequest_Returns200() throws Exception {
        when(attendanceService.getDailyHours(anyLong(), anyString())).thenReturn(8.0);
        mockMvc.perform(get("/attendance/daily-hours?employeeId=1&date=2024-06-01"))
                .andExpect(status().isOk())
                .andExpect(content().string("8.0"));
    }

    @Test
    @WithMockUser
    void testGetAttendanceReport_ValidDateRange_Returns200() throws Exception {
        when(attendanceService.getAttendanceReport(anyLong(), anyString(), anyString())).thenReturn("CSV_DATA");
        mockMvc.perform(get("/attendance/report?employeeId=1&startDate=2024-06-01&endDate=2024-06-07"))
                .andExpect(status().isOk())
                .andExpect(content().string("CSV_DATA"));
    }

    @Test
    @WithMockUser
    void testGetAttendanceReport_InvalidDateRange_Returns400() throws Exception {
        when(attendanceService.getAttendanceReport(anyLong(), anyString(), anyString())).thenThrow(new IllegalArgumentException("Invalid date range"));
        mockMvc.perform(get("/attendance/report?employeeId=1&startDate=2024-06-07&endDate=2024-06-01"))
                .andExpect(status().isBadRequest());
    }
}
