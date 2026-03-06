import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TimeAttendanceTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void clockInSuccess() throws Exception {
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":123,"location":"HQ"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("clocked_in"));
    }

    @Test
    void clockOutSuccess() throws Exception {
        mockMvc.perform(post("/attendance/clock-out")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":123,"location":"HQ"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("clocked_out"));
    }

    @Test
    void geofenceValidationFails() throws Exception {
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":123,"location":"remote"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Geofence validation failed"));
    }

    @Test
    void calculateHoursWorked() throws Exception {
        mockMvc.perform(get("/attendance/hours-worked?employeeId=123&date=2024-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hours").value(8));
    }

    @Test
    void missedPunchDetected() throws Exception {
        mockMvc.perform(get("/attendance/missed-punches?employeeId=123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missed").isArray());
    }

    @Test
    void correctionWorkflowInitiated() throws Exception {
        mockMvc.perform(post("/attendance/correction-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":123,"date":"2024-06-01","reason":"Missed punch"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("pending"));
    }

    @Test
    void csvReportExport() throws Exception {
        mockMvc.perform(get("/attendance/export-csv?employeeId=123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"));
    }
}
