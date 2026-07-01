package com.kodelabs.formflow.modules.forms.application.usecase.response;

import com.kodelabs.formflow.modules.forms.application.service.CandidateScoringService;
import com.kodelabs.formflow.modules.forms.application.service.ConditionalLogicEvaluator;
import com.kodelabs.formflow.modules.forms.application.service.FormSnapshotBuilder;
import com.kodelabs.formflow.modules.forms.application.service.ScoringResult;
import com.kodelabs.formflow.modules.forms.domain.model.AnswerValue;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateResponseSubmittedEvent;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateScores;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.port.in.SubmitCandidateResponseUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AnswerItem;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.SubmitCandidateResponseCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SubmitCandidateResponseResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
public class SubmitCandidateResponseService implements SubmitCandidateResponseUseCase, ApplicationEventPublisherAware {

    private final CandidateRepositoryPort candidateRepository;
    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final FormLoader formLoader;
    private final FormResponseRepositoryPort responseRepository;
    private final FormSnapshotBuilder snapshotBuilder;
    private final ConditionalLogicEvaluator conditionalLogicEvaluator;
    private final CandidateScoringService candidateScoringService;

    private ApplicationEventPublisher eventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public SubmitCandidateResponseResult execute(SubmitCandidateResponseCommand command) {
        Candidate candidate = loadInvitedCandidate(command.candidateToken());
        Convocatoria convocatoria = loadActiveConvocatoria(candidate.getConvocatoriaId(), candidate.getTenantId());
        Form form = loadFormWithQuestions(convocatoria.getFormId());
        Map<UUID, Object> answerMap = buildAnswerMap(command.answers());
        validateRequiredQuestions(form, answerMap);
        ScoringResult scoring = computeScoring(form, convocatoria, answerMap);
        FormResponse response = persistResponse(form, convocatoria, candidate, command);
        recordCandidateResponse(candidate, response, scoring);
        eventPublisher.publishEvent(new CandidateResponseSubmittedEvent(
                candidate.getId(), convocatoria.getId(), candidate.getTenantId()));
        return new SubmitCandidateResponseResult(response.getRespondentToken());
    }

    private Candidate loadInvitedCandidate(UUID token) {
        Candidate candidate = candidateRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("error.candidate.not_found", HttpStatus.NOT_FOUND, token));
        if (candidate.getStatus() != CandidateStatus.INVITED) {
            throw new BusinessException("error.candidate.already_responded", HttpStatus.CONFLICT);
        }
        return candidate;
    }

    private Convocatoria loadActiveConvocatoria(UUID convocatoriaId, UUID tenantId) {
        Convocatoria conv = convocatoriaRepository.findByIdAndTenantId(convocatoriaId, tenantId)
                .orElseThrow(() -> new BusinessException(
                        "error.convocatoria.not_found", HttpStatus.NOT_FOUND, convocatoriaId));
        if (!conv.isActive()) {
            throw new BusinessException("error.convocatoria.not_active", HttpStatus.CONFLICT);
        }
        return conv;
    }

    private Form loadFormWithQuestions(UUID formId) {
        return formLoader.loadPublicOrThrow(formId);
    }

    private ScoringResult computeScoring(Form form, Convocatoria convocatoria, Map<UUID, Object> answerMap) {
        return candidateScoringService.compute(form, convocatoria, answerMap);
    }

    private FormResponse persistResponse(Form form, Convocatoria convocatoria, Candidate candidate,
                                          SubmitCandidateResponseCommand command) {
        var snapshot = snapshotBuilder.buildFromForm(form);
        List<AnswerValue> answers = command.answers().stream().map(this::toAnswerValue).toList();
        return responseRepository.save(FormResponse.builder()
                .formId(form.getId())
                .tenantId(form.getTenantId())
                .convocatoriaId(convocatoria.getId())
                .candidateId(candidate.getId())
                .respondentToken(UUID.randomUUID())
                .formSnapshot(snapshot)
                .answers(answers)
                .startedAt(command.startedAt())
                .build());
    }

    private void recordCandidateResponse(Candidate candidate, FormResponse response, ScoringResult scoring) {
        Map<UUID, Double> byCategory = scoring.scoresByCategory().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().contribution()));
        candidate.setStatus(CandidateStatus.RESPONDED);
        candidate.setResponseId(response.getId());
        candidate.setScores(new CandidateScores(scoring.totalScore(), byCategory));
        candidate.setRespondedAt(Instant.now());
        candidateRepository.save(candidate);
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
                            HttpStatus.BAD_REQUEST, question.getTitle());
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
