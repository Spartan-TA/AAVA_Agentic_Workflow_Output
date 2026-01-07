package SpringBootTestSuite;

import com.example.customermanagement.controller.ReportController;
import com.example.customermanagement.exception.ResourceNotFoundException;
import com.example.customermanagement.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for ReportController covering sales report endpoint and admin validation.
 */
@WebMvcTest(ReportController.class)
public class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @BeforeEach
    public void setup() {
        Mockito.reset(reportService);
    }

    @Test
    public void testGenerateSalesReport_WithAdmin_ShouldReturnOk() throws Exception {
        when(reportService.generateSalesReport(1L)).thenReturn("Sales Report: ...");

        mockMvc.perform(get("/api/reports/sales")
                .param("adminId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Sales Report: ..."));
    }

    @Test
    public void testGenerateSalesReport_WithNonAdmin_ShouldReturnForbidden() throws Exception {
        when(reportService.generateSalesReport(2L)).thenThrow(new SecurityException("Not admin"));

        mockMvc.perform(get("/api/reports/sales")
                .param("adminId", "2"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGenerateSalesReport_WithInvalidUser_ShouldReturnNotFound() throws Exception {
        when(reportService.generateSalesReport(99L)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/reports/sales")
                .param("adminId", "99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGenerateSalesReport_WithMissingAdminId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/reports/sales"))
                .andExpect(status().isBadRequest());
    }
}
