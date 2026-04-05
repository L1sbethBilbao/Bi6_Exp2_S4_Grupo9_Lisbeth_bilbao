package com.duoc.backend.medication;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MedicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class MedicationControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MedicationService medicationService;

    @MockBean
    private JwtSigningKeyProvider jwtSigningKeyProvider;

    @BeforeEach
    void stubJwtKey() {
        SecretKey key = Keys.hmacShaKeyFor("TestOnly_Min32Chars_JWT_Signing_Key_XXX!!".getBytes(StandardCharsets.UTF_8));
        when(jwtSigningKeyProvider.getSecretKey()).thenReturn(key);
    }

    @Test
    void crudEndpoints() throws Exception {
        Medication m = new Medication();
        m.setId(1L);
        when(medicationService.getAllMedications()).thenReturn(List.of(m));
        mockMvc.perform(get("/medication")).andExpect(status().isOk());
        when(medicationService.getMedicationById(1L)).thenReturn(m);
        mockMvc.perform(get("/medication/1")).andExpect(status().isOk());
        when(medicationService.saveMedication(any())).thenReturn(m);
        mockMvc.perform(post("/medication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(m)))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/medication/1")).andExpect(status().isOk());
        verify(medicationService).deleteMedication(1L);
    }
}
