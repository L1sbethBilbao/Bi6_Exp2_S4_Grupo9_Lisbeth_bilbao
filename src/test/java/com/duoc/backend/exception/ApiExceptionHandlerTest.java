package com.duoc.backend.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiExceptionHandlerTest {

    private static final String JSON_ERROR = "$.error";

    private MockMvc mockMvc;

    @RestController
    static class ThrowingController {
        @GetMapping("/probe/nf")
        public void notFound() {
            throw new NoSuchElementException("missing");
        }

        @GetMapping("/probe/bad")
        public void badRequest() {
            throw new IllegalArgumentException("invalid");
        }

        @GetMapping("/probe/conflict")
        public void conflict() {
            throw new IllegalStateException("state");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void mapsNoSuchElementTo404() throws Exception {
        mockMvc.perform(get("/probe/nf").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(JSON_ERROR).value("missing"));
    }

    @Test
    void mapsIllegalArgumentTo400() throws Exception {
        mockMvc.perform(get("/probe/bad").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(JSON_ERROR).value("invalid"));
    }

    @Test
    void mapsIllegalStateTo409() throws Exception {
        mockMvc.perform(get("/probe/conflict").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath(JSON_ERROR).value("state"));
    }
}
