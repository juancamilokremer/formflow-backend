package com.kodelabs.formflow.modules.forms.infrastructure.persistence;

import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.TextConfig;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards #122. {@code @CreationTimestamp}/{@code @UpdateTimestamp} are before-execution
 * generators: Hibernate fills them in right before the INSERT runs, not on persist. Every
 * adapter here used to map the saved entity straight to a DTO with a plain {@code save()}, so
 * the returned entity still had null timestamps and the API answered {@code createdAt: null}.
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
}
