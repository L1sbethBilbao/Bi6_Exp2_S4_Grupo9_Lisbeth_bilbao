package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyUserDetailsServiceLoadTest {

    private static final String USERNAME_ADMIN = "admin";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MyUserDetailsService myUserDetailsService;

    @Test
    void loadUserByUsernameFound() {
        User u = new User();
        u.setUsername(USERNAME_ADMIN);
        when(userRepository.findByUsername(USERNAME_ADMIN)).thenReturn(u);
        UserDetails details = myUserDetailsService.loadUserByUsername(USERNAME_ADMIN);
        assertThat(details.getUsername()).isEqualTo(USERNAME_ADMIN);
        assertThat(details.getAuthorities()).isNotEmpty();
    }

    @Test
    void loadUserByUsernameNotFound() {
        when(userRepository.findByUsername("x")).thenReturn(null);
        assertThatThrownBy(() -> myUserDetailsService.loadUserByUsername("x"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
