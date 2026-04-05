package com.duoc.backend.care;

import com.duoc.backend.security.JwtSigningKeyProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CareController.class)
@AutoConfigureMockMvc(addFilters = false)
class CareControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CareRepository careRepository;

    @MockBean
    private JwtSigningKeyProvider jwtSigningKeyProvider;

    @BeforeEach
    void stubJwtKey() {
        SecretKey key = Keys.hmacShaKeyFor("TestOnly_Min32Chars_JWT_Signing_Key_XXX!!".getBytes(StandardCharsets.UTF_8));
        when(jwtSigningKeyProvider.getSecretKey()).thenReturn(key);
    }

    @Test
    void crudEndpoints() throws Exception {
        Care c = new Care();
        c.setId(1L);
        when(careRepository.findAll()).thenReturn(List.of(c));
        mockMvc.perform(get("/care")).andExpect(status().isOk());
        when(careRepository.findById(1L)).thenReturn(Optional.of(c));
        mockMvc.perform(get("/care/1")).andExpect(status().isOk());
        when(careRepository.save(c)).thenReturn(c);
        mockMvc.perform(post("/care")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(c)))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/care/1")).andExpect(status().isOk());
        verify(careRepository).deleteById(1L);
    }
}
