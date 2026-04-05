package com.duoc.backend;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConstantsTest {

    @Test
    void loginAndAuthConstants() {
        assertThat(Constants.LOGIN_URL).isEqualTo("/login");
        assertThat(Constants.HEADER_AUTHORIZACION_KEY).isEqualTo("Authorization");
        assertThat(Constants.TOKEN_BEARER_PREFIX).isEqualTo("Bearer ");
    }
}
