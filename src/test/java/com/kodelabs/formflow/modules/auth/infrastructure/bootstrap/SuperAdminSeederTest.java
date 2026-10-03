package com.kodelabs.formflow.modules.auth.infrastructure.bootstrap;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.out.PasswordHasherPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuperAdminSeederTest {

    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private UserRepositoryPort userRepository;
    @Mock private PasswordHasherPort passwordHasher;
    @Mock private ConfigurableApplicationContext applicationContext;

    private SuperAdminSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new SuperAdminSeeder(tenantRepository, userRepository, passwordHasher, applicationContext);
        seeder.tenantSlug = "kodelabs";
        seeder.tenantName = "Kode Labs";
        seeder.email = "admin@kodelabs.com";
        seeder.password = "a-real-password";
        seeder.firstName = "Ada";
        seeder.lastName = "Lovelace";
    }

    @Test
    void reportsAllMissingRequiredFieldsAtOnce() {
        seeder.email = "";
        seeder.password = "";
        seeder.firstName = "";
        seeder.lastName = "";

        assertThat(seeder.missingRequiredFields())
                .containsExactlyInAnyOrder("seed.email", "seed.password", "seed.first-name", "seed.last-name");
    }

    @Test
    void reportsNoMissingFieldsWhenAllProvided() {
        assertThat(seeder.missingRequiredFields()).isEmpty();
    }

    @Test
    void createsThePlatformTenantWhenItDoesNotExist() {
        when(tenantRepository.findBySlug("kodelabs")).thenReturn(Optional.empty());
        when(tenantRepository.save(any())).thenAnswer(inv -> {
            Tenant t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });
        when(userRepository.findByEmailAndTenantId(any(), any())).thenReturn(Optional.empty());
        when(passwordHasher.hash("a-real-password")).thenReturn("$2a$hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        seeder.seed();

        ArgumentCaptor<Tenant> tenantCaptor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(tenantCaptor.capture());
        assertThat(tenantCaptor.getValue().getSlug()).isEqualTo("kodelabs");
        assertThat(tenantCaptor.getValue().getName()).isEqualTo("Kode Labs");
    }

    @Test
    void reusesThePlatformTenantWhenItAlreadyExists() {
        UUID tenantId = UUID.randomUUID();
        Tenant existingTenant = Tenant.builder().id(tenantId).slug("kodelabs").name("Kode Labs").build();
        when(tenantRepository.findBySlug("kodelabs")).thenReturn(Optional.of(existingTenant));
        when(userRepository.findByEmailAndTenantId(any(), any())).thenReturn(Optional.empty());
        when(passwordHasher.hash(any())).thenReturn("$2a$hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        seeder.seed();

        verify(tenantRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void createsANewSuperAdminWithTheRealPasswordHash() {
        UUID tenantId = UUID.randomUUID();
        Tenant existingTenant = Tenant.builder().id(tenantId).slug("kodelabs").name("Kode Labs").build();
        when(tenantRepository.findBySlug("kodelabs")).thenReturn(Optional.of(existingTenant));
        when(userRepository.findByEmailAndTenantId("admin@kodelabs.com", tenantId)).thenReturn(Optional.empty());
        when(passwordHasher.hash("a-real-password")).thenReturn("$2a$realhash");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        seeder.seed();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("admin@kodelabs.com");
        assertThat(saved.getPasswordHash()).isEqualTo("$2a$realhash");
        assertThat(saved.getRole()).isEqualTo(UserRole.SUPER_ADMIN);
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.isEmailVerified()).isTrue();
        assertThat(saved.getTenantId()).isEqualTo(tenantId);
    }

    @Test
    void updatesAnExistingAccountInsteadOfFailing() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Tenant existingTenant = Tenant.builder().id(tenantId).slug("kodelabs").name("Kode Labs").build();
        User existingUser = User.builder()
                .id(userId).tenantId(tenantId).email("admin@kodelabs.com")
                .passwordHash("$2a$oldhash").firstName("Old").lastName("Name")
                .role(UserRole.SUPER_ADMIN).active(true).emailVerified(true)
                .build();
        when(tenantRepository.findBySlug("kodelabs")).thenReturn(Optional.of(existingTenant));
        when(userRepository.findByEmailAndTenantId("admin@kodelabs.com", tenantId)).thenReturn(Optional.of(existingUser));
        when(passwordHasher.hash("a-real-password")).thenReturn("$2a$newhash");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        seeder.seed();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getId()).isEqualTo(userId);
        assertThat(saved.getPasswordHash()).isEqualTo("$2a$newhash");
        assertThat(saved.getFirstName()).isEqualTo("Ada");
        assertThat(saved.getLastName()).isEqualTo("Lovelace");
    }

    @Test
    void normalizesTheEmailToLowercase() {
        UUID tenantId = UUID.randomUUID();
        Tenant existingTenant = Tenant.builder().id(tenantId).slug("kodelabs").name("Kode Labs").build();
        seeder.email = "  Admin@KodeLabs.com ";
        when(tenantRepository.findBySlug("kodelabs")).thenReturn(Optional.of(existingTenant));
        when(userRepository.findByEmailAndTenantId("admin@kodelabs.com", tenantId)).thenReturn(Optional.empty());
        when(passwordHasher.hash(any())).thenReturn("$2a$hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        seeder.seed();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("admin@kodelabs.com");
    }
}
