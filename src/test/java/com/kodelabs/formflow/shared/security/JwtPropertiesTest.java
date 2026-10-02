package com.kodelabs.formflow.shared.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

class JwtPropertiesTest {

    @Test
    void failsFastWhenSecretIsNull() {
        JwtProperties properties = new JwtProperties();

        assertThatThrownBy(properties::validateSecretLength)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    void failsFastWhenSecretIsShorterThan32Characters() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("demasiado-corto");

        assertThatThrownBy(properties::validateSecretLength)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    void acceptsASecretOfExactly32Characters() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("a".repeat(32));

        assertThatNoException().isThrownBy(properties::validateSecretLength);
    }
}
