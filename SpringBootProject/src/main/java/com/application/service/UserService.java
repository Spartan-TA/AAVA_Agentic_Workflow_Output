package com.application.service;

import com.application.dto.request.UserRequestDTO;
import com.application.dto.response.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponseDTO createUser(UserRequestDTO requestDTO);
    UserResponseDTO getUserById(Long id);
    UserResponseDTO updateUser(Long id, UserRequestDTO requestDTO);
    void deleteUser(Long id);
    Page<UserResponseDTO> getAllUsers(Pageable pageable);
    UserResponseDTO getUserByUsername(String username);
}
