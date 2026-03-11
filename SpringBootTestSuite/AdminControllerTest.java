import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

@WebMvcTest(AdminController.class)
public class AdminControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AdminService adminService;
    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User("admin", "admin@email.com", "pass");
    }

    @Test
    public void testGetAllUsers() throws Exception {
        List<User> users = Arrays.asList(user, new User("u2", "u2@email.com", "p2"));
        when(adminService.getAllUsers()).thenReturn(users);
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin"));
    }

    @Test
    public void testDeleteUser_Success() throws Exception {
        doNothing().when(adminService).deleteUser(1L);
        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteUser_NotFound() throws Exception {
        doThrow(new NoSuchElementException()).when(adminService).deleteUser(1L);
        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateUserRole_Success() throws Exception {
        User updated = new User("admin", "admin@email.com", "pass");
        updated.setRole("MODERATOR");
        when(adminService.updateUserRole(1L, "MODERATOR")).thenReturn(updated);
        mockMvc.perform(put("/admin/users/1/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""MODERATOR""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MODERATOR"));
    }

    @Test
    public void testUpdateUserRole_NotFound() throws Exception {
        when(adminService.updateUserRole(1L, "MODERATOR")).thenThrow(new NoSuchElementException());
        mockMvc.perform(put("/admin/users/1/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""MODERATOR""))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }
}
