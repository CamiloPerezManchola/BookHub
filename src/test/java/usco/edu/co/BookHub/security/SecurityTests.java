package usco.edu.co.BookHub.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectedEndpointWithoutLogin() throws Exception {

        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "USER")
    void normalUserCannotAccessAdminEndpoint() throws Exception {

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessUserManagement() throws Exception {

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk());
    }

    @Test
    void registrationWithoutCsrfIsRejected() throws Exception {

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }
}