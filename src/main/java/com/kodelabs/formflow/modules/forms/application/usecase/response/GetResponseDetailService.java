package com.kodelabs.formflow.modules.forms.application.usecase.response;

import com.kodelabs.formflow.modules.forms.domain.model.AnswerValue;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetResponseDetailUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetResponseDetailQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.AnswerValueResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponseDetailResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetResponseDetailService implements GetResponseDetailUseCase {

    private final FormRepositoryPort formRepository;
    private final FormResponseRepositoryPort responseRepository;
    private final CandidateRepositoryPort candidateRepository;

    @Override
    @Transactional(readOnly = true)
    public ResponseDetailResult execute(GetResponseDetailQuery query) {
        validateFormBelongsToTenant(query.formId(), query.tenantId());
        FormResponse response = loadResponseInForm(query.responseId(), query.formId(), query.tenantId());
        Double totalScore = resolveScore(response.getCandidateId());
        List<AnswerValueResult> answers = response.getAnswers().stream()
                .map(this::toAnswerValueResult)
                .toList();
        return new ResponseDetailResult(
                response.getId(), response.getFormId(), response.getRespondentToken(),
                response.getConvocatoriaId(), response.getCandidateId(), totalScore,
                response.getFormSnapshot(), answers, response.getSubmittedAt(), response.getStartedAt());
    }

    private void validateFormBelongsToTenant(UUID formId, UUID tenantId) {
        formRepository.findByIdAndTenantId(formId, tenantId)
                .orElseThrow(() -> new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND, formId));
    }

    private FormResponse loadResponseInForm(UUID responseId, UUID formId, UUID tenantId) {
        FormResponse response = responseRepository.findByIdAndTenantId(responseId, tenantId)
                .orElseThrow(() -> new BusinessException("error.response.not_found", HttpStatus.NOT_FOUND, responseId));
        if (!formId.equals(response.getFormId())) {
            throw new BusinessException("error.response.not_found", HttpStatus.NOT_FOUND, responseId);
        }
        return response;
    }

    private Double resolveScore(UUID candidateId) {
        if (candidateId == null) return null;
        return candidateRepository.findAllByIds(List.of(candidateId)).stream()
                .findFirst()
                .map(Candidate::getScores)
                .map(scores -> scores.total())
                .orElse(null);
    }

    private AnswerValueResult toAnswerValueResult(AnswerValue answer) {
        return new AnswerValueResult(answer.getQuestionId(), answer.getValue());
    }
}
