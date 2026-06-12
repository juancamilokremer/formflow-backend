package com.kodelabs.formflow.modules.auth.domain.port;

/**
 * Output port for password hashing.
 * The implementation (BCrypt) lives in infrastructure.
 */
public interface PasswordHasherPort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
