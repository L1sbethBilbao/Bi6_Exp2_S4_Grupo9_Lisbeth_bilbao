package com.duoc.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private JWTAuthenticationConfig jwtAuthenticationConfig;
    @Mock
    private MyUserDetailsService userDetailsService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginController loginController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(loginController).build();
    }

    @Test
    void login_success() throws Exception {
        User u = new User();
        u.setUsername("admin");
        u.setPassword("hash");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(u);
        when(passwordEncoder.matches("admin123", "hash")).thenReturn(true);
        when(jwtAuthenticationConfig.getJWTToken("admin")).thenReturn("Bearer eyJ.test");

        mockMvc.perform(post("/login").param("user", "admin").param("password", "admin123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Bearer eyJ.test"));
    }

    @Test
    void login_legacyEncryptedPass() throws Exception {
        User u = new User();
        when(userDetailsService.loadUserByUsername("u")).thenReturn(u);
        when(passwordEncoder.matches("secret", u.getPassword())).thenReturn(true);
        when(jwtAuthenticationConfig.getJWTToken("u")).thenReturn("Bearer x");

        mockMvc.perform(post("/login").param("user", "u").param("encryptedPass", "secret"))
                .andExpect(status().isOk());
    }

    @Test
    void login_badRequest_whenNoPassword() throws Exception {
        mockMvc.perform(post("/login").param("user", "admin"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_unauthorized_wrongPassword() throws Exception {
        User u = new User();
        u.setPassword("hash");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(u);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/login").param("user", "admin").param("password", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_unauthorized_userNotFound() throws Exception {
        when(userDetailsService.loadUserByUsername("nobody"))
                .thenThrow(new UsernameNotFoundException("nobody"));

        mockMvc.perform(post("/login").param("user", "nobody").param("password", "x"))
                .andExpect(status().isUnauthorized());
    }
}
