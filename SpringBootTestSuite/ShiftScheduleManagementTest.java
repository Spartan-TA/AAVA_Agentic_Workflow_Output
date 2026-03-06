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
class ShiftScheduleManagementTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createShiftTemplate() throws Exception {
        mockMvc.perform(post("/shift/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"Morning","start":"08:00","end":"16:00"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Morning"));
    }

    @Test
    void updateShiftTemplate() throws Exception {
        mockMvc.perform(put("/shift/templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"Evening","start":"16:00","end":"00:00"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Evening"));
    }

    @Test
    void deleteShiftTemplate() throws Exception {
        mockMvc.perform(delete("/shift/templates/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shiftRotationAssigned() throws Exception {
        mockMvc.perform(post("/shift/rotations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":123,"rotation":"A-B-C"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rotation").value("A-B-C"));
    }

    @Test
    void conflictDetectionWorks() throws Exception {
        mockMvc.perform(post("/shift/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":123,"shiftId":1,"date":"2024-06-01"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Shift conflict detected"));
    }

    @Test
    void bulkAssignmentSuccess() throws Exception {
        mockMvc.perform(post("/shift/bulk-assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{"employeeId":123,"shiftId":1},{"employeeId":124,"shiftId":2}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigned").isArray());
    }

    @Test
    void getPersonalUpcomingShifts() throws Exception {
        mockMvc.perform(get("/shift/upcoming?employeeId=123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shifts").isArray());
    }

    @Test
    void auditEntryCreated() throws Exception {
        mockMvc.perform(post("/shift/audit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"action":"create","shiftId":1,"user":"admin"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.audit").exists());
    }
}
