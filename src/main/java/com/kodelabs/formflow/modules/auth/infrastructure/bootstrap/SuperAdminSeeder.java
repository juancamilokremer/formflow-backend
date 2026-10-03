package com.kodelabs.formflow.modules.auth.infrastructure.bootstrap;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.out.PasswordHasherPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * One-off bootstrap command, never part of normal app startup: creates (or updates) the
 * platform's first SUPER_ADMIN account, hashed with the app's real PasswordEncoder instead
 * of a hand-crafted SQL INSERT (see backend#179 — the only way to get this role today is a
 * manual INSERT with an improvised hash).
 *
 * Run it, don't deploy it running: activate the "seed-super-admin" profile for a single
 * invocation against the target environment, e.g.
 *
 *   mvn spring-boot:run -Dspring-boot.run.profiles=seed-super-admin -Dspring-boot.run.arguments=\
 *     "--seed.email=admin@kodelabs.com --seed.password=... --seed.first-name=Ada --seed.last-name=Lovelace"
 *
 * or, against a packaged jar (e.g. a one-off Railway shell):
 *
 *   java -jar app.jar --spring.profiles.active=seed-super-admin --seed.email=... --seed.password=... \
 *     --seed.first-name=... --seed.last-name=...
 *
 * Safe to re-run: if the account already exists it's updated (password rotation, name fix)
 * instead of failing. The SUPER_ADMIN always lives in a dedicated internal tenant
 * ("kodelabs" by default, overridable with --seed.tenant-slug/--seed.tenant-name) — never
 * inside a real customer's tenant — created on first run if it doesn't exist yet.
 */
@Slf4j
@Component
@Profile("seed-super-admin")
@RequiredArgsConstructor
public class SuperAdminSeeder implements CommandLineRunner {

    private final TenantRepositoryPort tenantRepository;
    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final ConfigurableApplicationContext applicationContext;

    // Package-private (not private) so SuperAdminSeederTest can set them directly —
    // @Value field injection only runs inside a real Spring context.
    @Value("${seed.tenant-slug:kodelabs}")
    String tenantSlug;

    @Value("${seed.tenant-name:Kode Labs}")
    String tenantName;

    @Value("${seed.email:}")
    String email;

    @Value("${seed.password:}")
    String password;

    @Value("${seed.first-name:}")
    String firstName;

    @Value("${seed.last-name:}")
    String lastName;

    @Override
    public void run(String... args) {
        int exitCode = 0;
        try {
            List<String> missing = missingRequiredFields();
            if (!missing.isEmpty()) {
                log.error("Faltan argumentos obligatorios: {}. Uso: --seed.email=... --seed.password=... "
                        + "--seed.first-name=... --seed.last-name=... (opcional: --seed.tenant-slug, --seed.tenant-name)",
                        String.join(", ", missing));
                exitCode = 1;
            } else {
                seed();
            }
        } catch (Exception e) {
            log.error("Fallo creando/actualizando la cuenta SUPER_ADMIN", e);
            exitCode = 1;
        }
        int finalExitCode = exitCode;
        System.exit(SpringApplication.exit(applicationContext, () -> finalExitCode));
    }

    // Package-private (not private) so SuperAdminSeederTest can exercise the real logic
    // directly, without going through run()'s System.exit() path.
    List<String> missingRequiredFields() {
        List<String> missing = new ArrayList<>();
        if (email.isBlank()) missing.add("seed.email");
        if (password.isBlank()) missing.add("seed.password");
        if (firstName.isBlank()) missing.add("seed.first-name");
        if (lastName.isBlank()) missing.add("seed.last-name");
        return missing;
    }

    void seed() {
        Tenant tenant = tenantRepository.findBySlug(tenantSlug)
                .orElseGet(this::createPlatformTenant);

        String normalizedEmail = email.toLowerCase().trim();
        User user = userRepository.findByEmailAndTenantId(normalizedEmail, tenant.getId())
                .map(existing -> {
                    log.info("La cuenta '{}' ya existe en el tenant '{}' — actualizando.", normalizedEmail, tenantSlug);
                    return existing;
                })
                .orElseGet(User::new);

        user.setTenantId(tenant.getId());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordHasher.hash(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(UserRole.SUPER_ADMIN);
        user.setActive(true);
        user.setEmailVerified(true);

        userRepository.save(user);
        log.info("Cuenta SUPER_ADMIN lista — tenantSlug='{}' email='{}'", tenantSlug, normalizedEmail);
    }

    private Tenant createPlatformTenant() {
        log.info("Tenant interno '{}' no existe — creándolo.", tenantSlug);
        return tenantRepository.save(Tenant.builder()
                .slug(tenantSlug)
                .name(tenantName)
                .acceptedTermsAt(Instant.now())
                .build());
    }
}
