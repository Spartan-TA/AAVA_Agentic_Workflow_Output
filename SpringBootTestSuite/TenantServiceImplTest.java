package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class TenantServiceImplTest {

    @Mock
    private TenantRepository tenantRepository;
    @InjectMocks
    private TenantServiceImpl tenantService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    @DisplayName("createTenant - valid input - tenant created")
    void testCreateTenant_ValidInput_TenantCreated() {
        Tenant tenant = new Tenant(null, "Acme", "ACME123");
        when(tenantRepository.save(any())).thenAnswer(i -> {
            Tenant t = i.getArgument(0);
            t.setId(1L);
            return t;
        });
        Tenant result = tenantService.createTenant(tenant);
        assertNotNull(result.getId());
        assertEquals("Acme", result.getName());
    }

    @Test
    @DisplayName("getTenantById - found - returns tenant")
    void testGetTenantById_Found_ReturnsTenant() {
        Tenant tenant = new Tenant(1L, "Acme", "ACME123");
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        Tenant result = tenantService.getTenantById(1L);
        assertEquals("Acme", result.getName());
    }

    @Test
    @DisplayName("getTenantById - not found - throws exception")
    void testGetTenantById_NotFound_ThrowsException() {
        when(tenantRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(TenantNotFoundException.class, () -> tenantService.getTenantById(2L));
    }

    @Test
    @DisplayName("updateTenant - valid - tenant updated")
    void testUpdateTenant_Valid_TenantUpdated() {
        Tenant tenant = new Tenant(1L, "Acme", "ACME123");
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        Tenant updated = new Tenant(1L, "Acme Corp", "ACME123");
        when(tenantRepository.save(any())).thenReturn(updated);
        Tenant result = tenantService.updateTenant(1L, updated);
        assertEquals("Acme Corp", result.getName());
    }

    @Test
    @DisplayName("updateTenant - not found - throws exception")
    void testUpdateTenant_NotFound_ThrowsException() {
        when(tenantRepository.findById(2L)).thenReturn(Optional.empty());
        Tenant updated = new Tenant(2L, "Acme", "ACME123");
        assertThrows(TenantNotFoundException.class, () -> tenantService.updateTenant(2L, updated));
    }

    @Test
    @DisplayName("deleteTenant - valid - tenant deleted")
    void testDeleteTenant_Valid_TenantDeleted() {
        Tenant tenant = new Tenant(1L, "Acme", "ACME123");
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        doNothing().when(tenantRepository).deleteById(1L);
        assertDoesNotThrow(() -> tenantService.deleteTenant(1L));
    }

    @Test
    @DisplayName("deleteTenant - not found - throws exception")
    void testDeleteTenant_NotFound_ThrowsException() {
        when(tenantRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(TenantNotFoundException.class, () -> tenantService.deleteTenant(2L));
    }

    @Test
    @DisplayName("getTenantByCode - found - returns tenant")
    void testGetTenantByCode_Found_ReturnsTenant() {
        Tenant tenant = new Tenant(1L, "Acme", "ACME123");
        when(tenantRepository.findByCode("ACME123")).thenReturn(Optional.of(tenant));
        Tenant result = tenantService.getTenantByCode("ACME123");
        assertEquals("Acme", result.getName());
    }

    @Test
    @DisplayName("getTenantByCode - not found - throws exception")
    void testGetTenantByCode_NotFound_ThrowsException() {
        when(tenantRepository.findByCode("XYZ")).thenReturn(Optional.empty());
        assertThrows(TenantNotFoundException.class, () -> tenantService.getTenantByCode("XYZ"));
    }
}