package com.example.warehouse.controller;

import com.example.warehouse.dto.ShiftTemplateDto;
import com.example.warehouse.service.SchedulingService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SchedulingControllerTest {

    @Mock private SchedulingService schedulingService;
    @InjectMocks private SchedulingController schedulingController;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(schedulingController).build();
    }

    @Test
    void createShiftTemplate_ShouldReturnCreatedTemplate() throws Exception {
        ShiftTemplateDto dto = new ShiftTemplateDto();
        dto.setId(1L);
        dto.setName("Morning Shift");

        when(schedulingService.createShiftTemplate(any())).thenReturn(dto);

        mockMvc.perform(post("/scheduling/shift-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"Morning Shift","startTime":"08:00","endTime":"16:00"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Morning Shift"));
    }

    @Test
    void getAllShiftTemplates_ShouldReturnList() throws Exception {
        ShiftTemplateDto dto = new ShiftTemplateDto();
        dto.setId(1L);
        dto.setName("Morning Shift");

        when(schedulingService.getAllShiftTemplates()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/scheduling/shift-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Morning Shift"));
    }

    @Test
    void updateShiftTemplate_ShouldReturnUpdatedTemplate() throws Exception {
        ShiftTemplateDto dto = new ShiftTemplateDto();
        dto.setId(1L);
        dto.setName("Morning Shift Updated");

        when(schedulingService.updateShiftTemplate(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/scheduling/shift-templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"Morning Shift Updated","startTime":"08:00","endTime":"16:00"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Morning Shift Updated"));
    }

    @Test
    void deleteShiftTemplate_ShouldReturnNoContent() throws Exception {
        doNothing().when(schedulingService).deleteShiftTemplate(1L);

        mockMvc.perform(delete("/scheduling/shift-templates/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void createShiftTemplate_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/scheduling/shift-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"","startTime":"invalid","endTime":"invalid"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getShiftTemplateById_ShouldReturnTemplate() throws Exception {
        ShiftTemplateDto dto = new ShiftTemplateDto();
        dto.setId(1L);
        dto.setName("Morning Shift");

        when(schedulingService.getShiftTemplateById(1L)).thenReturn(dto);

        mockMvc.perform(get("/scheduling/shift-templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Morning Shift"));
    }

    @Test
    void getShiftTemplateById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        when(schedulingService.getShiftTemplateById(99L)).thenThrow(new ResourceNotFoundException("Shift template not found"));

        mockMvc.perform(get("/scheduling/shift-templates/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateShiftTemplate_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        when(schedulingService.updateShiftTemplate(eq(99L), any())).thenThrow(new ResourceNotFoundException("Shift template not found"));

        mockMvc.perform(put("/scheduling/shift-templates/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"Morning Shift","startTime":"08:00","endTime":"16:00"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteShiftTemplate_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Shift template not found")).when(schedulingService).deleteShiftTemplate(99L);

        mockMvc.perform(delete("/scheduling/shift-templates/99"))
                .andExpect(status().isNotFound());
    }
}