package com.duoc.backend.invoice;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InvoiceController.class)
@AutoConfigureMockMvc(addFilters = false)
class InvoiceControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private JwtSigningKeyProvider jwtSigningKeyProvider;

    @BeforeEach
    void stubJwtKey() {
        SecretKey key = Keys.hmacShaKeyFor("TestOnly_Min32Chars_JWT_Signing_Key_XXX!!".getBytes(StandardCharsets.UTF_8));
        when(jwtSigningKeyProvider.getSecretKey()).thenReturn(key);
    }

    @Test
    void endpoints() throws Exception {
        Invoice inv = new Invoice();
        inv.setId(1L);
        when(invoiceService.getAllInvoices()).thenReturn(List.of(inv));
        mockMvc.perform(get("/invoice")).andExpect(status().isOk());
        when(invoiceService.getInvoicesByAppointmentId(2L)).thenReturn(List.of(inv));
        mockMvc.perform(get("/invoice/visit/2")).andExpect(status().isOk());
        when(invoiceService.getInvoiceById(1L)).thenReturn(Optional.of(inv));
        mockMvc.perform(get("/invoice/1")).andExpect(status().isOk());
        when(invoiceService.getInvoiceById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/invoice/99")).andExpect(status().isNotFound());
        when(invoiceService.saveInvoice(any())).thenReturn(inv);
        mockMvc.perform(post("/invoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated());
        mockMvc.perform(delete("/invoice/1")).andExpect(status().isNoContent());
        verify(invoiceService).deleteInvoice(1L);
    }
}
