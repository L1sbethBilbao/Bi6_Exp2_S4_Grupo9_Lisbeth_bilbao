package com.duoc.backend.patient;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PatientController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatientControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    @MockBean
    private JwtSigningKeyProvider jwtSigningKeyProvider;

    @BeforeEach
    void stubJwtKey() {
        SecretKey key = Keys.hmacShaKeyFor("TestOnly_Min32Chars_JWT_Signing_Key_XXX!!".getBytes(StandardCharsets.UTF_8));
        when(jwtSigningKeyProvider.getSecretKey()).thenReturn(key);
    }

    @Test
    void registerGreeting() throws Exception {
        mockMvc.perform(get("/patient/register").param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello {Test}"));
    }

    @Test
    void listPatients() throws Exception {
        Patient p = new Patient();
        p.setId(1L);
        p.setName("n");
        when(patientService.getAllPatients()).thenReturn(List.of(p));
        mockMvc.perform(get("/patient"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("n"));
    }

    @Test
    void getByIdSaveDelete() throws Exception {
        Patient p = new Patient();
        p.setId(2L);
        when(patientService.getPatientById(2L)).thenReturn(p);
        mockMvc.perform(get("/patient/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));

        when(patientService.savePatient(any())).thenAnswer(i -> i.getArgument(0));
        mockMvc.perform(post("/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/patient/2"))
                .andExpect(status().isOk());
        verify(patientService).deletePatient(2L);
    }
}
