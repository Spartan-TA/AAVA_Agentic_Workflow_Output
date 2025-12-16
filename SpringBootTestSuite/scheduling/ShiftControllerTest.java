package com.companyname.wems.scheduling.controller;

import com.companyname.wems.scheduling.service.ShiftService;
import com.companyname.wems.scheduling.dto.ShiftDTO;
import com.companyname.wems.scheduling.dto.AssignShiftRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShiftController.class)
class ShiftControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShiftService shiftService;

    @Autowired
    private ObjectMapper objectMapper;

    private ShiftDTO testShiftDTO;
    private AssignShiftRequest validAssignRequest;

    @BeforeEach
    void setUp() {
        testShiftDTO = new ShiftDTO();
        testShiftDTO.setId(1L);
        testShiftDTO.setEmployeeId(100L);
        testShiftDTO.setStart("2023-06-01T08:00:00");
        testShiftDTO.setEnd("2023-06-01T16:00:00");

        validAssignRequest = new AssignShiftRequest();
        validAssignRequest.setEmployeeId(100L);
        validAssignRequest.setStart("2023-06-01T08:00:00");
        validAssignRequest.setEnd("2023-06-01T16:00:00");
    }

    @Test
    void testAssignShift_ValidInput_Returns201() throws Exception {
        when(shiftService.assignShift(any(AssignShiftRequest.class))).thenReturn(testShiftDTO);
        mockMvc.perform(post("/shifts/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAssignRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId").value(100));
    }

    @Test
    void testAssignShift_Conflict_Returns409() throws Exception {
        when(shiftService.assignShift(any(AssignShiftRequest.class))).thenThrow(new ShiftConflictException("Conflict detected"));
        mockMvc.perform(post("/shifts/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAssignRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void testAssignShift_InvalidInput_Returns400() throws Exception {
        validAssignRequest.setStart("");
        mockMvc.perform(post("/shifts/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAssignRequest)))
                .andExpect(status().isBadRequest());
    }
}
