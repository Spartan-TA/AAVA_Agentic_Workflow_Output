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

import com.example.controller.ShiftController;
import com.example.service.ShiftService;

@WebMvcTest(ShiftController.class)
public class ShiftControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShiftService shiftService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShiftTemplate_ValidRequest_Returns201() throws Exception {
        when(shiftService.createShiftTemplate(any())).thenReturn(1L);
        mockMvc.perform(post("/shifts/templates")
                .contentType("application/json")
                .content("{"name":"Night"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void testCreateShiftTemplate_Unauthorized_Returns401() throws Exception {
        mockMvc.perform(post("/shifts/templates")
                .contentType("application/json")
                .content("{"name":"Night"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAssignShift_ValidRequest_Returns201() throws Exception {
        when(shiftService.assignShift(anyLong(), anyLong(), anyString())).thenReturn(1L);
        mockMvc.perform(post("/shifts/assign")
                .contentType("application/json")
                .content("{"employeeId":1,"shiftTemplateId":1,"date":"2024-06-01"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAssignShift_Conflict_Returns409() throws Exception {
        when(shiftService.assignShift(anyLong(), anyLong(), anyString())).thenThrow(new IllegalStateException("Conflict"));
        mockMvc.perform(post("/shifts/assign")
                .contentType("application/json")
                .content("{"employeeId":1,"shiftTemplateId":1,"date":"2024-06-01"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void testGetEmployeeShifts_Returns200() throws Exception {
        when(shiftService.getEmployeeShifts(anyLong())).thenReturn(java.util.Collections.emptyList());
        mockMvc.perform(get("/shifts/employee/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetShiftById_Exists_Returns200() throws Exception {
        when(shiftService.getShiftById(1L)).thenReturn(java.util.Optional.of(new Object()));
        mockMvc.perform(get("/shifts/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetShiftById_NotFound_Returns404() throws Exception {
        when(shiftService.getShiftById(2L)).thenReturn(java.util.Optional.empty());
        mockMvc.perform(get("/shifts/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteShift_AdminRole_Returns204() throws Exception {
        doNothing().when(shiftService).deleteShift(1L);
        mockMvc.perform(delete("/shifts/1"))
                .andExpect(status().isNoContent());
    }
}
