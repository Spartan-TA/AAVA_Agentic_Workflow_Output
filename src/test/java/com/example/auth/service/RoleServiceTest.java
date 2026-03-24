package com.example.auth.service;

import com.example.auth.entity.Role;
import com.example.auth.repository.RoleRepository;
import com.example.auth.exception.RoleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private AutoCloseable closeable;
    private Role testRole;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("ROLE_USER");
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testFindByName_ExistingRole_ReturnsRole() {
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(testRole));
        Role role = roleService.findByName("ROLE_USER");
        assertNotNull(role);
        assertEquals("ROLE_USER", role.getName());
    }

    @Test
    void testFindByName_NonExistingRole_ThrowsException() {
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.empty());
        assertThrows(RoleException.class, () -> roleService.findByName("ROLE_ADMIN"));
    }

    @Test
    void testFindByName_NullName_ThrowsException() {
        assertThrows(RoleException.class, () -> roleService.findByName(null));
    }

    @Test
    void testFindByName_EmptyName_ThrowsException() {
        assertThrows(RoleException.class, () -> roleService.findByName(""));
    }

    @Test
    void testCreateRole_ValidRole_Success() {
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);
        Role created = roleService.createRole("ROLE_USER");
        assertNotNull(created);
        assertEquals("ROLE_USER", created.getName());
    }

    @Test
    void testCreateRole_NullName_ThrowsException() {
        assertThrows(RoleException.class, () -> roleService.createRole(null));
    }

    @Test
    void testCreateRole_EmptyName_ThrowsException() {
        assertThrows(RoleException.class, () -> roleService.createRole(""));
    }

    @Test
    void testGetAllRoles_ReturnsList() {
        when(roleRepository.findAll()).thenReturn(List.of(testRole));
        List<Role> roles = roleService.getAllRoles();
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("ROLE_USER", roles.get(0).getName());
    }

    @Test
    void testDeleteRole_ValidId_Success() {
        doNothing().when(roleRepository).deleteById(1L);
        assertDoesNotThrow(() -> roleService.deleteRole(1L));
        verify(roleRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteRole_NullId_ThrowsException() {
        assertThrows(RoleException.class, () -> roleService.deleteRole(null));
    }

    @Test
    void testDeleteRole_NonExistingId_ThrowsException() {
        doThrow(new RoleException("Role not found")).when(roleRepository).deleteById(2L);
        assertThrows(RoleException.class, () -> roleService.deleteRole(2L));
    }
}
