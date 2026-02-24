package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.time.*;

class WorkflowServiceTest {

    @Mock
    private WorkflowRepository workflowRepository;
    @Mock
    private AssetRepository assetRepository;
    @InjectMocks
    private WorkflowService workflowService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void testStartOnboarding_Valid() {
        Workflow wf = new Workflow(1L, "ONBOARDING", "STARTED");
        when(workflowRepository.save(any(Workflow.class))).thenReturn(wf);
        Workflow result = workflowService.startOnboarding(1L);
        assertNotNull(result);
        assertEquals("STARTED", result.getStatus());
    }

    @Test
    void testCompleteTask_UnauthorizedUser() {
        Task task = new Task(1L, "ONBOARDING", 2L);
        when(workflowRepository.findTaskById(1L)).thenReturn(Optional.of(task));
        Exception ex = assertThrows(SecurityException.class, () ->
            workflowService.completeTask(1L, 99L));
        assertEquals("User not authorized to complete task", ex.getMessage());
    }

    @Test
    void testRevokeAccess_ActiveEmployee() {
        Employee emp = new Employee(1L, true);
        when(workflowRepository.findEmployeeById(1L)).thenReturn(Optional.of(emp));
        Exception ex = assertThrows(IllegalStateException.class, () ->
            workflowService.revokeAccess(1L));
        assertEquals("Cannot revoke access for active employee", ex.getMessage());
    }

    @Test
    void testAllocateAssets_InsufficientInventory() {
        when(assetRepository.findAvailableAssets(anyList())).thenReturn(Collections.emptyList());
        Exception ex = assertThrows(IllegalStateException.class, () ->
            workflowService.allocateAssets(1L, Arrays.asList(1L,2L)));
        assertEquals("Insufficient inventory", ex.getMessage());
    }

    @Test
    void testWorkflowStateTransitions() {
        Workflow wf = new Workflow(1L, "ONBOARDING", "STARTED");
        when(workflowRepository.findById(1L)).thenReturn(Optional.of(wf));
        wf.setStatus("COMPLETED");
        when(workflowRepository.save(any(Workflow.class))).thenReturn(wf);
        Workflow result = workflowService.startOnboarding(1L);
        assertEquals("COMPLETED", result.getStatus());
    }
}