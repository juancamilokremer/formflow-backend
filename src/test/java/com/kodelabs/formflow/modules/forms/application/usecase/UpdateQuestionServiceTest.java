package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.service.QuestionConfigFactory;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.TextConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateQuestionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateQuestionServiceTest {

    @Mock private FormQuestionRepositoryPort questionRepository;
    @Mock private QuestionConfigFactory configFactory;
    @InjectMocks private UpdateQuestionService service;

    @Test
    void updatesAllFieldsAndReturnsResult() {
        UUID questionId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        FormQuestion question = FormQuestion.builder().id(questionId).sectionId(sectionId)
                .tenantId(tenantId).title("Vieja").type(new QuestionType("TEXT")).position(0).build();
        when(questionRepository.findByIdAndSectionIdAndTenantId(questionId, sectionId, tenantId))
                .thenReturn(Optional.of(question));
        when(configFactory.build(any(), any())).thenReturn(new TextConfig());
        when(questionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuestionResult result = service.execute(new UpdateQuestionCommand(
                questionId, sectionId, UUID.randomUUID(), tenantId, UUID.randomUUID(),
                "Nueva", "Desc", new QuestionType("TEXT"), true, null, null, Map.of()));

        assertThat(result.title()).isEqualTo("Nueva");
        assertThat(result.required()).isTrue();
    }

    @Test
    void throwsNotFoundWhenQuestionDoesNotExist() {
        UUID questionId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        when(questionRepository.findByIdAndSectionIdAndTenantId(questionId, sectionId, tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new UpdateQuestionCommand(
                questionId, sectionId, UUID.randomUUID(), tenantId, UUID.randomUUID(),
                "T", null, new QuestionType("TEXT"), false, null, null, Map.of())))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.question.not_found")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}
