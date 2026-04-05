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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    private static final String ADMIN_USER;
    private static final String ADMIN_PLAIN;
    private static final String LEGACY_PLAIN;
    private static final String WRONG_PLAIN;
    private static final String OTHER_USER;
    private static final String OTHER_PLAIN;

    static {
        Properties props = new Properties();
        try (InputStream in = LoginControllerTest.class.getResourceAsStream("/application-test.properties")) {
            if (in == null) {
                throw new IllegalStateException("classpath:application-test.properties no encontrado");
            }
            props.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
        ADMIN_USER = require(props, "app.test.login.admin-user");
        ADMIN_PLAIN = require(props, "app.test.login.admin-plain");
        LEGACY_PLAIN = require(props, "app.test.login.legacy-plain");
        WRONG_PLAIN = require(props, "app.test.login.wrong-plain");
        OTHER_PLAIN = require(props, "app.test.login.other-plain");
        OTHER_USER = "nobody";
    }

    private static String require(Properties p, String key) {
        String v = p.getProperty(key);
        if (v == null) {
            throw new IllegalStateException("Falta propiedad " + key);
        }
        return v;
    }

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
    void loginSuccess() throws Exception {
        User u = new User();
        u.setUsername(ADMIN_USER);
        u.setPassword("hash");
        when(userDetailsService.loadUserByUsername(ADMIN_USER)).thenReturn(u);
        when(passwordEncoder.matches(ADMIN_PLAIN, "hash")).thenReturn(true);
        when(jwtAuthenticationConfig.getJWTToken(ADMIN_USER)).thenReturn("Bearer eyJ.test");

        mockMvc.perform(post("/login").param("user", ADMIN_USER).param("password", ADMIN_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string("Bearer eyJ.test"));
    }

    @Test
    void loginLegacyEncryptedPass() throws Exception {
        User u = new User();
        when(userDetailsService.loadUserByUsername("u")).thenReturn(u);
        when(passwordEncoder.matches(LEGACY_PLAIN, u.getPassword())).thenReturn(true);
        when(jwtAuthenticationConfig.getJWTToken("u")).thenReturn("Bearer x");

        mockMvc.perform(post("/login").param("user", "u").param("encryptedPass", LEGACY_PLAIN))
                .andExpect(status().isOk());
    }

    @Test
    void loginBadRequestWhenNoPassword() throws Exception {
        mockMvc.perform(post("/login").param("user", ADMIN_USER))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginUnauthorizedWrongPassword() throws Exception {
        User u = new User();
        u.setPassword("hash");
        when(userDetailsService.loadUserByUsername(ADMIN_USER)).thenReturn(u);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/login").param("user", ADMIN_USER).param("password", WRONG_PLAIN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginUnauthorizedUserNotFound() throws Exception {
        when(userDetailsService.loadUserByUsername(OTHER_USER))
                .thenThrow(new UsernameNotFoundException(OTHER_USER));

        mockMvc.perform(post("/login").param("user", OTHER_USER).param("password", OTHER_PLAIN))
                .andExpect(status().isUnauthorized());
    }
}
