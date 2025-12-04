package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class ShiftTemplateServiceTest {

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @InjectMocks
    private ShiftTemplateService shiftTemplateService;

    private ShiftTemplateDTO validTemplateDTO;
    private ShiftTemplate validTemplate;

    @BeforeEach
    public void setUp() {
        validTemplateDTO = new ShiftTemplateDTO();
        validTemplateDTO.setName("Morning Shift");
        validTemplateDTO.setStartTime(LocalTime.of(8, 0));
        validTemplateDTO.setEndTime(LocalTime.of(16, 0));
        validTemplateDTO.setRecurring(true);
        validTemplateDTO.setRotationPattern("WEEKLY");

        validTemplate = new ShiftTemplate();
        validTemplate.setId(1L);
        validTemplate.setName("Morning Shift");
        validTemplate.setStartTime(LocalTime.of(8, 0));
        validTemplate.setEndTime(LocalTime.of(16, 0));
        validTemplate.setRecurring(true);
        validTemplate.setRotationPattern("WEEKLY");
    }

    @Test
    public void testCreateTemplate_ValidInput_Success() {
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(validTemplate);

        ShiftTemplateDTO result = shiftTemplateService.createTemplate(validTemplateDTO);

        assertNotNull(result);
        assertEquals("Morning Shift", result.getName());
        assertEquals(LocalTime.of(8, 0), result.getStartTime());
        assertEquals(LocalTime.of(16, 0), result.getEndTime());
    }

    @Test
    public void testCreateTemplate_StartTimeAfterEndTime_ThrowsException() {
        validTemplateDTO.setStartTime(LocalTime.of(16, 0));
        validTemplateDTO.setEndTime(LocalTime.of(8, 0));

        assertThrows(IllegalArgumentException.class, () -> shiftTemplateService.createTemplate(validTemplateDTO));
    }

    @Test
    public void testCreateTemplate_NullName_ThrowsException() {
        validTemplateDTO.setName(null);

        assertThrows(Exception.class, () -> shiftTemplateService.createTemplate(validTemplateDTO));
    }

    @Test
    public void testGetAllTemplates_ReturnsAllTemplates() {
        when(shiftTemplateRepository.findAll()).thenReturn(Collections.singletonList(validTemplate));

        List<ShiftTemplateDTO> result = shiftTemplateService.getAllTemplates();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Morning Shift", result.get(0).getName());
    }
}