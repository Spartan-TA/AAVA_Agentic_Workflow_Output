import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class AssetControllerTest {
    private MockMvc mockMvc;

    @Mock
    private AssetService assetService;

    @InjectMocks
    private AssetController assetController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(assetController).build();
    }

    @Test
    public void testAssignAsset_ValidInput_ReturnsCreated() throws Exception {
        AssetDto dto = new AssetDto("A123", "Scanner", "EMP123");
        when(assetService.assignAsset(any())).thenReturn(new Asset("A123", "Scanner", "EMP123", java.time.LocalDate.now(), null, "Good"));
        mockMvc.perform(post("/asset/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"assetId":"A123","type":"Scanner","assignedTo":"EMP123"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assignedTo").value("EMP123"));
    }

    @Test
    public void testAssignAsset_NullInput_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/asset/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testReturnAsset_WithCondition_ReturnsOk() throws Exception {
        when(assetService.returnAsset(any(), eq("Damaged"))).thenReturn(new Asset("A123", "Scanner", "EMP123", java.time.LocalDate.now().minusDays(5), java.time.LocalDate.now(), "Damaged"));
        mockMvc.perform(post("/asset/return")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"assetId":"A123","condition":"Damaged"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.condition").value("Damaged"));
    }

    @Test
    public void testReturnAsset_MissingCondition_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/asset/return")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"assetId":"A123"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetOverdueAssets_ReturnsOk() throws Exception {
        when(assetService.getOverdueAssets(7)).thenReturn(java.util.Collections.emptyList());
        mockMvc.perform(get("/asset/overdue/7"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void testConditionTracking_BoundaryConditions() throws Exception {
        Asset asset = new Asset("A125", "PPE", "EMP125", java.time.LocalDate.now(), null, "Excellent");
        asset.setCondition("Poor");
        assertEquals("Poor", asset.getCondition());
    }
}
