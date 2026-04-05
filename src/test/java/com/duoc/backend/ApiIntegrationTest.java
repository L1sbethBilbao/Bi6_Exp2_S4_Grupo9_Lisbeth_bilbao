package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JWTAuthenticationConfig jwtAuthenticationConfig;

    @Test
    void jwtConfig_emitsBearerPrefixedToken() {
        assertThat(jwtAuthenticationConfig.getJWTToken("anyUser")).startsWith("Bearer ");
    }

    @Test
    void login_withDefaultAdmin_thenAccessProtectedEndpoint() throws Exception {
        String token = mockMvc.perform(post("/login")
                        .param("user", "admin")
                        .param("password", "admin123"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(get("/patient").header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void protectedPath_withMalformedJwt_returnsForbidden() throws Exception {
        mockMvc.perform(get("/patient").header("Authorization", "Bearer not-a-valid-jwt"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedPath_withoutAuth_returnsForbidden() throws Exception {
        mockMvc.perform(get("/care"))
                .andExpect(status().isForbidden());
    }
}
