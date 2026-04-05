package com.duoc.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecuredControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SecuredController()).build();
    }

    @Test
    void greetings_defaultName() throws Exception {
        mockMvc.perform(get("/greetings"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello {World}"));
    }

    @Test
    void greetings_customName() throws Exception {
        mockMvc.perform(get("/greetings").param("name", "Ana"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello {Ana}"));
    }
}
