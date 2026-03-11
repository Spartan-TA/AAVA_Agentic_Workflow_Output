package com.example.demo;

import com.example.demo.controller.UserController;
import com.example.demo.domain.User;
import com.example.demo.dto.UserEditRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("View Profile")
    class ViewProfileTests {
        @Test
        @WithMockUser(username = "user1")
        void getProfile_withValidUser_returnsProfile() throws Exception {
            UserResponse response = new UserResponse(1L, "user1", "user1@email.com", "ACTIVE");
            Mockito.when(userService.getProfile("user1")).thenReturn(response);
            mockMvc.perform(get("/api/users/profile"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("user1"));
        }

        @Test
        void getProfile_withoutAuth_returnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/users/profile"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Edit Profile")
    class EditProfileTests {
        @Test
        @WithMockUser(username = "user1")
        void editProfile_withValidInput_returnsUpdatedProfile() throws Exception {
            UserEditRequest request = new UserEditRequest("newemail@email.com", "ACTIVE");
            UserResponse response = new UserResponse(1L, "user1", "newemail@email.com", "ACTIVE");
            Mockito.when(userService.editProfile(eq("user1"), any(UserEditRequest.class))).thenReturn(response);
            mockMvc.perform(put("/api/users/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("newemail@email.com"));
        }

        @Test
        @WithMockUser(username = "user1")
        void editProfile_withInvalidEmail_returnsBadRequest() throws Exception {
            UserEditRequest request = new UserEditRequest("bademail", "ACTIVE");
            mockMvc.perform(put("/api/users/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "user1")
        void editProfile_withNonexistentUser_returnsNotFound() throws Exception {
            UserEditRequest request = new UserEditRequest("user@email.com", "ACTIVE");
            Mockito.when(userService.editProfile(eq("user1"), any(UserEditRequest.class))).thenThrow(new UserNotFoundException());
            mockMvc.perform(put("/api/users/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }
}
