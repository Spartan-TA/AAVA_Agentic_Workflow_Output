package com.example.warehouse.service;

import com.example.warehouse.entity.Role;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;
import com.example.warehouse.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing roles.
 */
@Service
public class RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Get all roles.
     * @return List of Role
     */
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * Get role by ID.
     * @param id Role ID
     * @return Role
     */
    @Transactional(readOnly = true)
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    /**
     * Create a new role.
     * @param name Role name
     * @return Role
     */
    @Transactional
    public Role createRole(String name) {
        if (name == null || name.isEmpty()) {
            throw new ValidationException("Role name is required");
        }
        if (roleRepository.existsByName(name)) {
            throw new ValidationException("Role name already exists");
        }
        Role role = new Role();
        role.setName(name);
        return roleRepository.save(role);
    }

    /**
     * Delete a role by ID.
     * @param id Role ID
     */
    @Transactional
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role not found with id: " + id);
        }
        roleRepository.deleteById(id);
    }
}
