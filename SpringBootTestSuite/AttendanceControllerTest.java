package SpringBootTestSuite;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

@WebMvcTest(AttendanceController.class)
public class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    private AttendanceDTO validClockInDTO;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        validClockInDTO = new AttendanceDTO();
        validClockInDTO.setEmployeeId(1L);
        validClockInDTO.setDeviceId("DEV001");
        validClockInDTO.setLocation("Main Gate");
        validClockInDTO.setTimestamp(LocalDateTime.now());
        validClockInDTO.setType(EventType.CLOCK_IN);
    }

    @Test
    public void testRecordClockIn_ValidRequest_Returns201() throws Exception {
        when(attendanceService.recordClockIn(any(AttendanceDTO.class))).thenReturn(validClockInDTO);

        mockMvc.perform(post("/api/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validClockInDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId").value(1L))
                .andExpect(jsonPath("$.type").value("CLOCK_IN"));
    }

    @Test
    public void testRecordClockIn_InvalidRequest_Returns400() throws Exception {
        AttendanceDTO invalidDTO = new AttendanceDTO();
        invalidDTO.setEmployeeId(null);

        mockMvc.perform(post("/api/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetEmployeeAttendance_ValidId_Returns200() throws Exception {
        when(attendanceService.getEmployeeAttendance(eq(1L), any(), any()))
                .thenReturn(Collections.singletonList(validClockInDTO));

        mockMvc.perform(get("/api/attendance/employee/1")
                .param("startDate", "2023-01-01")
                .param("endDate", "2023-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeId").value(1L));
    }
}