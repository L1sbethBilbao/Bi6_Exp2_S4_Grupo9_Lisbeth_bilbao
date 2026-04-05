package com.duoc.backend;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class LoginController {

    private final JWTAuthenticationConfig jwtAuthenticationConfig;
    private final MyUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public LoginController(
            JWTAuthenticationConfig jwtAuthenticationConfig,
            MyUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        this.jwtAuthenticationConfig = jwtAuthenticationConfig;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Autenticación: contraseña en claro vía {@code password} (recomendado) o {@code encryptedPass} (compatibilidad con el ejemplo del curso).
     * La contraseña se compara con el hash almacenado (BCrypt).
     */
    @PostMapping("/login")
    public String login(
            @RequestParam("user") String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "encryptedPass", required = false) String legacyPassword) {

        String rawPassword = (password != null && !password.isEmpty()) ? password : legacyPassword;
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe enviar password o encryptedPass");
        }

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
            }
            return jwtAuthenticationConfig.getJWTToken(username);
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
    }
}
