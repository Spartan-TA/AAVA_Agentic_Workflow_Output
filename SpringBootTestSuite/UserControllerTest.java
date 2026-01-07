package SpringBootTestSuite;

import com.example.customermanagement.controller.UserController;
import com.example.customermanagement.dto.UserProfileUpdateDto;
import com.example.customermanagement.dto.UserRegistrationDto;
import com.example.customermanagement.entity.User;
import com.example.customermanagement.exception.ResourceNotFoundException;
import com.example.customermanagement.exception.UserAlreadyExistsException;
import com.example.customermanagement.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for UserController covering registration, verification, profile update, and retrieval.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        Mockito.reset(userService);
    }

    @Test
    public void testRegisterUser_WithValidData_ShouldReturnCreated() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto("test@example.com", "Password123", "Test User");
        User user = new User(); user.setEmail("test@example.com");
        when(userService.registerUser(any(UserRegistrationDto.class))).thenReturn(user);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    public void testRegisterUser_WithExistingEmail_ShouldReturnConflict() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto("existing@example.com", "Password123", "Test User");
        when(userService.registerUser(any(UserRegistrationDto.class))).thenThrow(new UserAlreadyExistsException("Email exists"));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testRegisterUser_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto("invalid-email", "Password123", "Test User");
        when(userService.registerUser(any(UserRegistrationDto.class))).thenThrow(new IllegalArgumentException("Invalid email"));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testVerifyUser_WithValidToken_ShouldReturnOk() throws Exception {
        when(userService.verifyUser("valid-token")).thenReturn(true);

        mockMvc.perform(get("/api/users/verify?token=valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Verification successful"));
    }

    @Test
    public void testVerifyUser_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
        when(userService.verifyUser("invalid-token")).thenReturn(false);

        mockMvc.perform(get("/api/users/verify?token=invalid-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateProfile_WithValidData_ShouldReturnOk() throws Exception {
        UserProfileUpdateDto dto = new UserProfileUpdateDto("New Name", "1234567890");
        User user = new User(); user.setId(1L); user.setName("New Name");
        when(userService.updateProfile(anyLong(), any(UserProfileUpdateDto.class))).thenReturn(user);

        mockMvc.perform(put("/api/users/1/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    public void testUpdateProfile_WithInvalidPhone_ShouldReturnBadRequest() throws Exception {
        UserProfileUpdateDto dto = new UserProfileUpdateDto("Name", "invalid-phone");
        when(userService.updateProfile(anyLong(), any(UserProfileUpdateDto.class))).thenThrow(new IllegalArgumentException("Invalid phone"));

        mockMvc.perform(put("/api/users/1/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetUserById_WithValidId_ShouldReturnOk() throws Exception {
        User user = new User(); user.setId(1L); user.setEmail("test@example.com");
        when(userService.findById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    public void testGetUserById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        when(userService.findById(99L)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }
}
