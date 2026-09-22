package com.kodelabs.formflow.modules.forms.application.usecase.response;

import com.kodelabs.formflow.modules.forms.application.service.ConditionalLogicEvaluator;
import com.kodelabs.formflow.modules.forms.application.service.FormSnapshotBuilder;
import com.kodelabs.formflow.modules.forms.domain.model.AnswerValue;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.port.in.SubmitPublicResponseUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AnswerItem;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.SubmitPublicResponseCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SubmitPublicResponseResult;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaFormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmitPublicResponseService implements SubmitPublicResponseUseCase {

    private final FormLoader formLoader;
    private final FormResponseRepositoryPort responseRepository;
    private final FormSnapshotBuilder snapshotBuilder;
    private final ConditionalLogicEvaluator conditionalLogicEvaluator;
    private final ConvocatoriaFormRepositoryPort convocatoriaFormRepository;
    private final ConvocatoriaRepositoryPort convocatoriaRepository;

    @Override
    @Transactional
    public SubmitPublicResponseResult execute(SubmitPublicResponseCommand command) {
        Form form = loadActiveForm(command.formId());
        Map<UUID, Object> answerMap = buildAnswerMap(command.answers());
        validateRequiredQuestions(form, answerMap);
        UUID convocatoriaId = resolveAnonymousEncuestaConvocatoriaId(form);
        UUID respondentToken = persistResponse(form, command, convocatoriaId);
        return new SubmitPublicResponseResult(respondentToken);
    }

    /** An anonymous submission still counts toward an encuesta's stats/response list when the
     *  form happens to be attached to one — candidateId stays null, it's genuinely anonymous.
     *  Restricted to REGISTRATION: a scored candidatura response with no candidate to rank makes
     *  no sense for CANDIDATES/DIAGNOSTIC convocatorias. */
    private UUID resolveAnonymousEncuestaConvocatoriaId(Form form) {
        return convocatoriaFormRepository.findAllByFormId(form.getId()).stream()
                .map(cf -> convocatoriaRepository.findByIdAndTenantId(cf.getConvocatoriaId(), form.getTenantId()))
                .flatMap(Optional::stream)
                .filter(c -> c.getType() == FormType.REGISTRATION)
                .findFirst()
                .map(convocatoria -> {
                    if (convocatoria.isClosed()) {
                        throw new BusinessException("error.convocatoria.closed", HttpStatus.CONFLICT);
                    }
                    return convocatoria.getId();
                })
                .orElse(null);
    }

    private Form loadActiveForm(UUID formId) {
        Form form = formLoader.loadPublicOrThrow(formId);
        if (form.getStatus() != FormStatus.ACTIVE) {
            throw new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND, formId);
        }
        return form;
    }

    private UUID persistResponse(Form form, SubmitPublicResponseCommand command, UUID convocatoriaId) {
        var snapshot = snapshotBuilder.buildFromForm(form);
        List<AnswerValue> answers = command.answers().stream()
                .map(this::toAnswerValue)
                .toList();
        UUID respondentToken = UUID.randomUUID();
        responseRepository.save(FormResponse.builder()
                .formId(form.getId())
                .tenantId(form.getTenantId())
                .convocatoriaId(convocatoriaId)
                .respondentToken(respondentToken)
                .formSnapshot(snapshot)
                .answers(answers)
                .startedAt(command.startedAt())
                .build());
        return respondentToken;
    }

    private AnswerValue toAnswerValue(AnswerItem item) {
        return AnswerValue.builder()
                .questionId(item.questionId())
                .value(item.value())
                .build();
    }

    private Map<UUID, Object> buildAnswerMap(List<AnswerItem> items) {
        Map<UUID, Object> map = new HashMap<>();
        items.forEach(item -> map.put(item.questionId(), item.value()));
        return map;
    }

    private void validateRequiredQuestions(Form form, Map<UUID, Object> answerMap) {
        for (FormSection section : form.getSections()) {
            for (FormQuestion question : section.getQuestions()) {
                boolean visible = conditionalLogicEvaluator.isVisible(
                        question.getConditionalLogic(), answerMap);
                if (visible && question.isRequired() && !isAnswered(answerMap, question.getId())) {
                    throw new BusinessException(
                            "error.response.required_question_empty",
                            HttpStatus.BAD_REQUEST,
                            question.getTitle());
                }
            }
        }
    }

    private boolean isAnswered(Map<UUID, Object> answerMap, UUID questionId) {
        if (!answerMap.containsKey(questionId)) return false;
        Object value = answerMap.get(questionId);
        if (value == null) return false;
        if (value instanceof String s) return !s.isBlank();
        if (value instanceof List<?> l) return !l.isEmpty();
        return true;
    }
}
