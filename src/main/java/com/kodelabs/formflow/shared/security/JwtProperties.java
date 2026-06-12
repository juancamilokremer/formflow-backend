package com.kodelabs.formflow.shared.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT configuration properties (app.jwt prefix in application.yml).
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {

    /** HMAC secret used to sign tokens. Minimum 32 characters. */
    private String secret;

    /** Access token validity in milliseconds. */
    private long expirationMs;

    /** Refresh token validity in milliseconds. */
    private long refreshExpirationMs;
}
