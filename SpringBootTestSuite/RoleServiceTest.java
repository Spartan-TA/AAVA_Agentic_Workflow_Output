package SpringBootTestSuite;

import com.example.usermanagement.entity.Role;
import com.example.usermanagement.exception.ResourceNotFoundException;
import com.example.usermanagement.repository.RoleRepository;
import com.example.usermanagement.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RoleServiceTest {
    @Mock
    private RoleRepository roleRepository;
    @InjectMocks
    private RoleService roleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Normal case: Create role with valid name
    @Test
    void testCreateRole_WithValidName_Success() {
        Role role = new Role();
        role.setName("ADMIN");
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        Role result = roleService.createRole("ADMIN");
        assertNotNull(result);
        assertEquals("ADMIN", result.getName());
    }

    // Edge case: Create role with duplicate name
    @Test
    void testCreateRole_WithDuplicateName_ThrowsException() {
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> roleService.createRole("ADMIN"));
    }

    // Boundary case: Create role with null name
    @Test
    void testCreateRole_WithNullName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> roleService.createRole(null));
    }

    // Boundary case: Create role with empty name
    @Test
    void testCreateRole_WithEmptyName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> roleService.createRole(""));
    }

    // Normal case: Find role by name
    @Test
    void testFindRoleByName_WithValidName_Success() {
        Role role = new Role();
        role.setName("USER");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        Role result = roleService.findRoleByName("USER");
        assertNotNull(result);
        assertEquals("USER", result.getName());
    }

    // Edge case: Find role by name not found
    @Test
    void testFindRoleByName_NotFound_ThrowsException() {
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> roleService.findRoleByName("MANAGER"));
    }

    // Normal case: Assign role to user
    @Test
    void testAssignRoleToUser_Success() {
        // Assume implementation assigns role to user
        // This is a placeholder for actual logic
        assertDoesNotThrow(() -> roleService.assignRoleToUser(1L, "ADMIN"));
    }

    // Edge case: Assign role to non-existent user
    @Test
    void testAssignRoleToUser_NonExistentUser_ThrowsException() {
        // Assume implementation throws exception for non-existent user
        doThrow(new ResourceNotFoundException("User not found")).when(roleRepository).assignRoleToUser(99L, "ADMIN");
        assertThrows(ResourceNotFoundException.class, () -> roleService.assignRoleToUser(99L, "ADMIN"));
    }

    // Edge case: Assign non-existent role to user
    @Test
    void testAssignRoleToUser_NonExistentRole_ThrowsException() {
        doThrow(new ResourceNotFoundException("Role not found")).when(roleRepository).assignRoleToUser(1L, "NON_EXISTENT");
        assertThrows(ResourceNotFoundException.class, () -> roleService.assignRoleToUser(1L, "NON_EXISTENT"));
    }

    // Normal case: List all roles
    @Test
    void testListAllRoles_Success() {
        when(roleRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        assertNotNull(roleService.listAllRoles());
    }
}
