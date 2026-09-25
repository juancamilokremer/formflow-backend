package com.kodelabs.formflow.modules.forms.infrastructure.persistence;

import com.kodelabs.formflow.modules.auth.domain.model.EmailToken;
import com.kodelabs.formflow.modules.auth.domain.model.EmailTokenType;
import com.kodelabs.formflow.modules.auth.domain.model.RefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.out.EmailTokenRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.RefreshTokenRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.TextConfig;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.FormSnapshot;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaFormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards #122. {@code @CreationTimestamp}/{@code @UpdateTimestamp} are before-execution
 * generators: Hibernate fills them in right before the INSERT runs, not on persist. Every
 * adapter here used to map the saved entity straight to a DTO with a plain {@code save()}, so
 * the returned entity still had null timestamps and the API answered {@code createdAt: null}.
 *
 * The original #122 fix covered 4 entities (Form/Category/FormSection/FormQuestion) found by a
 * narrow grep instead of an exhaustive sweep. A QA pass on a fresh tenant turned up the same
 * null {@code createdAt} on a brand-new Convocatoria — every other entity annotated
 * {@code @CreationTimestamp} across both modules (auth + forms) had the identical gap. This
 * file now covers all twelve.
 *
 * Full application context (like FormFlowApplicationTests) rather than a hand-wired
 * {@code @DataJpaTest}, so this exercises the real port implementations wired the way
 * production does — reverting an adapter's {@code saveAndFlush} back to {@code save} fails the
 * matching case. {@code @Transactional} rolls each case back; nothing is left behind.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SaveReturnsGeneratedTimestampsTest {

    @Autowired private FormRepositoryPort formRepository;
    @Autowired private FormSectionRepositoryPort sectionRepository;
    @Autowired private FormQuestionRepositoryPort questionRepository;
    @Autowired private CategoryRepositoryPort categoryRepository;
    @Autowired private TenantRepositoryPort tenantRepository;
    @Autowired private UserRepositoryPort userRepository;
    @Autowired private EmailTokenRepositoryPort emailTokenRepository;
    @Autowired private RefreshTokenRepositoryPort refreshTokenRepository;
    @Autowired private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Autowired private ConvocatoriaFormRepositoryPort convocatoriaFormRepository;
    @Autowired private CandidateRepositoryPort candidateRepository;
    @Autowired private FormResponseRepositoryPort responseRepository;

    private final UUID tenantId = UUID.randomUUID();

    @Test
    void formComesBackWithTimestamps() {
        Form saved = formRepository.save(Form.builder()
                .tenantId(tenantId).name("Form").type(FormType.CANDIDATES).status(FormStatus.DRAFT).version(1)
                .build());

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void categoryComesBackWithTimestamps() {
        Category saved = categoryRepository.save(Category.builder()
                .tenantId(tenantId).name("Técnicas").color("#4F46E5")
                .build());

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void sectionComesBackWithTimestamps() {
        Form form = formRepository.save(Form.builder()
                .tenantId(tenantId).name("Form").type(FormType.CANDIDATES).status(FormStatus.DRAFT).version(1)
                .build());

        FormSection saved = sectionRepository.save(FormSection.builder()
                .formId(form.getId()).tenantId(tenantId).title("Sección").position(0)
                .build());

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void questionComesBackWithTimestamps() {
        Form form = formRepository.save(Form.builder()
                .tenantId(tenantId).name("Form").type(FormType.CANDIDATES).status(FormStatus.DRAFT).version(1)
                .build());
        FormSection section = sectionRepository.save(FormSection.builder()
                .formId(form.getId()).tenantId(tenantId).title("Sección").position(0)
                .build());

        FormQuestion saved = questionRepository.save(FormQuestion.builder()
                .formId(form.getId()).sectionId(section.getId()).tenantId(tenantId)
                .title("Pregunta").type(QuestionType.TEXT).position(0).required(false)
                .config(TextConfig.builder().build())
                .build());

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void tenantComesBackWithTimestamps() {
        Tenant saved = tenantRepository.save(Tenant.builder()
                .slug("qa-timestamps-" + UUID.randomUUID()).name("QA Tenant")
                .build());

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void userComesBackWithTimestamps() {
        User saved = userRepository.save(User.builder()
                .tenantId(tenantId).email("qa" + UUID.randomUUID() + "@example.com")
                .passwordHash("hash").firstName("QA").lastName("Tester")
                .build());

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void emailTokenComesBackWithTimestamps() {
        EmailToken saved = emailTokenRepository.save(EmailToken.builder()
                .userId(UUID.randomUUID()).tenantId(tenantId).tokenHash("hash")
                .type(EmailTokenType.EMAIL_VERIFICATION).expiresAt(Instant.now().plusSeconds(3600))
                .build());

        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void refreshTokenComesBackWithTimestamps() {
        RefreshToken saved = refreshTokenRepository.save(RefreshToken.builder()
                .userId(UUID.randomUUID()).tenantId(tenantId).tokenHash("hash")
                .expiresAt(Instant.now().plusSeconds(3600))
                .build());

        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void convocatoriaComesBackWithTimestamps() {
        Convocatoria saved = convocatoriaRepository.save(Convocatoria.builder()
                .tenantId(tenantId).name("Convocatoria").type(FormType.CANDIDATES)
                .build());

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void convocatoriaFormComesBackWithTimestamps() {
        Convocatoria convocatoria = convocatoriaRepository.save(Convocatoria.builder()
                .tenantId(tenantId).name("Convocatoria").type(FormType.CANDIDATES)
                .build());
        Form form = formRepository.save(Form.builder()
                .tenantId(tenantId).name("Form").type(FormType.CANDIDATES).status(FormStatus.DRAFT).version(1)
                .build());

        ConvocatoriaForm saved = convocatoriaFormRepository.save(ConvocatoriaForm.builder()
                .convocatoriaId(convocatoria.getId()).formId(form.getId())
                .build());

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void candidateComesBackWithTimestamps() {
        Convocatoria convocatoria = convocatoriaRepository.save(Convocatoria.builder()
                .tenantId(tenantId).name("Convocatoria").type(FormType.CANDIDATES)
                .build());

        Candidate saved = candidateRepository.save(Candidate.builder()
                .convocatoriaId(convocatoria.getId()).tenantId(tenantId)
                .name("Candidato").email("candidato" + UUID.randomUUID() + "@example.com")
                .build());

        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // --- Update-path regression: a second QA-pass finding, sibling to the insert-path bug
    // above. @CreationTimestamp only fires on INSERT; Hibernate leaves it untouched on UPDATE.
    // toEntity() must carry the domain object's createdAt through, or a save() on an
    // already-persisted row silently echoes createdAt back as null in the response — even
    // though the database itself is never touched (confirmed by hand against Postgres while
    // investigating: the DB row kept its real value, only the API response was wrong). Found
    // launching a real convocatoria during the QA pass: the launch response's createdAt came
    // back null despite the row being three hours old.

    @Test
    void convocatoriaCreatedAtSurvivesAnUpdate() {
        Convocatoria saved = convocatoriaRepository.save(Convocatoria.builder()
                .tenantId(tenantId).name("Convocatoria").type(FormType.CANDIDATES)
                .build());
        Instant original = saved.getCreatedAt();

        saved.setName("Convocatoria renombrada");
        Convocatoria updated = convocatoriaRepository.save(saved);

        assertThat(updated.getCreatedAt()).isEqualTo(original);
    }

    @Test
    void candidateCreatedAtSurvivesAnUpdate() {
        Convocatoria convocatoria = convocatoriaRepository.save(Convocatoria.builder()
                .tenantId(tenantId).name("Convocatoria").type(FormType.CANDIDATES)
                .build());
        Candidate saved = candidateRepository.save(Candidate.builder()
                .convocatoriaId(convocatoria.getId()).tenantId(tenantId)
                .name("Candidato").email("candidato" + UUID.randomUUID() + "@example.com")
                .build());
        Instant original = saved.getCreatedAt();

        saved.setName("Candidato renombrado");
        Candidate updated = candidateRepository.save(saved);

        assertThat(updated.getCreatedAt()).isEqualTo(original);
    }

    @Test
    void categoryCreatedAtSurvivesAnUpdate() {
        Category saved = categoryRepository.save(Category.builder()
                .tenantId(tenantId).name("Técnicas").color("#4F46E5")
                .build());
        Instant original = saved.getCreatedAt();

        saved.setName("Técnicas renombrada");
        Category updated = categoryRepository.save(saved);

        assertThat(updated.getCreatedAt()).isEqualTo(original);
    }

    @Test
    void convocatoriaFormCreatedAtSurvivesAnUpdate() {
        Convocatoria convocatoria = convocatoriaRepository.save(Convocatoria.builder()
                .tenantId(tenantId).name("Convocatoria").type(FormType.CANDIDATES)
                .build());
        Form form = formRepository.save(Form.builder()
                .tenantId(tenantId).name("Form").type(FormType.CANDIDATES).status(FormStatus.DRAFT).version(1)
                .build());
        ConvocatoriaForm saved = convocatoriaFormRepository.save(ConvocatoriaForm.builder()
                .convocatoriaId(convocatoria.getId()).formId(form.getId())
                .build());
        Instant original = saved.getCreatedAt();

        saved.setWeight(50);
        ConvocatoriaForm updated = convocatoriaFormRepository.save(saved);

        assertThat(updated.getCreatedAt()).isEqualTo(original);
    }

    @Test
    void formCreatedAtSurvivesAnUpdate() {
        Form saved = formRepository.save(Form.builder()
                .tenantId(tenantId).name("Form").type(FormType.CANDIDATES).status(FormStatus.DRAFT).version(1)
                .build());
        Instant original = saved.getCreatedAt();

        saved.setName("Form renombrado");
        Form updated = formRepository.save(saved);

        assertThat(updated.getCreatedAt()).isEqualTo(original);
    }

    @Test
    void sectionCreatedAtSurvivesAnUpdate() {
        Form form = formRepository.save(Form.builder()
                .tenantId(tenantId).name("Form").type(FormType.CANDIDATES).status(FormStatus.DRAFT).version(1)
                .build());
        FormSection saved = sectionRepository.save(FormSection.builder()
                .formId(form.getId()).tenantId(tenantId).title("Sección").position(0)
                .build());
        Instant original = saved.getCreatedAt();

        saved.setTitle("Sección renombrada");
        FormSection updated = sectionRepository.save(saved);

        assertThat(updated.getCreatedAt()).isEqualTo(original);
    }

    @Test
    void questionCreatedAtSurvivesAnUpdate() {
        Form form = formRepository.save(Form.builder()
                .tenantId(tenantId).name("Form").type(FormType.CANDIDATES).status(FormStatus.DRAFT).version(1)
                .build());
        FormSection section = sectionRepository.save(FormSection.builder()
                .formId(form.getId()).tenantId(tenantId).title("Sección").position(0)
                .build());
        FormQuestion saved = questionRepository.save(FormQuestion.builder()
                .formId(form.getId()).sectionId(section.getId()).tenantId(tenantId)
                .title("Pregunta").type(QuestionType.TEXT).position(0).required(false)
                .config(TextConfig.builder().build())
                .build());
        Instant original = saved.getCreatedAt();

        saved.setTitle("Pregunta renombrada");
        FormQuestion updated = questionRepository.save(saved);

        assertThat(updated.getCreatedAt()).isEqualTo(original);
    }

    @Test
    void formResponseComesBackWithTimestamps() {
        Form form = formRepository.save(Form.builder()
                .tenantId(tenantId).name("Form").type(FormType.REGISTRATION).status(FormStatus.ACTIVE).version(1)
                .build());
        FormSnapshot snapshot = new FormSnapshot(form.getId(), "Form", "REGISTRATION", 1, Instant.now(), List.of());

        FormResponse saved = responseRepository.save(FormResponse.builder()
                .formId(form.getId()).tenantId(tenantId).respondentToken(UUID.randomUUID())
                .formSnapshot(snapshot)
                .build());

        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
