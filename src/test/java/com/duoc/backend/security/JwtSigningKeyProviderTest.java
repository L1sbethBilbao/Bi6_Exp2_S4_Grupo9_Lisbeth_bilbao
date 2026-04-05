package com.duoc.backend.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtSigningKeyProviderTest {

    private static final String VALID_32_PLUS = "TestOnly_Min32Chars_JWT_Signing_Key_XXX!!";

    @Test
    void buildsKeyWhenSecretLongEnough() {
        JwtSigningKeyProvider provider = new JwtSigningKeyProvider(VALID_32_PLUS);
        assertThat(provider.getSecretKey()).isNotNull();
        assertThat(provider.getSecretKey().getAlgorithm()).isNotBlank();
    }

    @Test
    void rejectsShortSecret() {
        assertThatThrownBy(() -> new JwtSigningKeyProvider("short"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }

    @Test
    void rejectsNullSecret() {
        assertThatThrownBy(() -> new JwtSigningKeyProvider(null))
                .isInstanceOf(IllegalStateException.class);
    }
}
