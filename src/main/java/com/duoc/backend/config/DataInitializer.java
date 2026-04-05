package com.duoc.backend.config;

import com.duoc.backend.User;
import com.duoc.backend.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner seedDefaultUser(UserRepository users, PasswordEncoder passwordEncoder) {
        return args -> {
            if (users.findByUsername("admin") != null) {
                return;
            }
            User u = new User();
            u.setUsername("admin");
            u.setEmail("admin@localhost");
            u.setPassword(passwordEncoder.encode("admin123"));
            users.save(u);
            log.info("Usuario por defecto creado: admin / admin123 (cambiar en entornos reales).");
        };
    }
}
