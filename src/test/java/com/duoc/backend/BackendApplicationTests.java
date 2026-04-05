package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

	@Test
	void contextLoads() {
		// Verificación intencionalmente mínima: @SpringBootTest ya falla si el contexto no levanta.
	}

}
