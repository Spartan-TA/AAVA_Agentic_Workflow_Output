package com.example.service;

import com.example.model.Role;
import com.example.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindRoleByName() {
        Role role = new Role("ADMIN");
        when(roleRepository.findByName("ADMIN")).thenReturn(role);
        Role found = roleService.findRoleByName("ADMIN");
        assertNotNull(found);
        assertEquals("ADMIN", found.getName());
    }

    @Test
    void testFindRoleByNameNotFound() {
        when(roleRepository.findByName("USER")).thenReturn(null);
        Role found = roleService.findRoleByName("USER");
        assertNull(found);
    }

    @Test
    void testGetAllRoles() {
        List<Role> roles = Arrays.asList(new Role("ADMIN"), new Role("USER"));
        when(roleRepository.findAll()).thenReturn(roles);
        List<Role> result = roleService.getAllRoles();
        assertEquals(2, result.size());
        assertEquals("ADMIN", result.get(0).getName());
        assertEquals("USER", result.get(1).getName());
    }

    @Test
    void testSaveRole() {
        Role role = new Role("MANAGER");
        when(roleRepository.save(role)).thenReturn(role);
        Role saved = roleService.saveRole(role);
        assertNotNull(saved);
        assertEquals("MANAGER", saved.getName());
    }

    @Test
    void testDeleteRole() {
        Role role = new Role("TEMP");
        role.setId(1L);
        doNothing().when(roleRepository).deleteById(1L);
        roleService.deleteRole(1L);
        verify(roleRepository).deleteById(1L);
    }
}
