package com.kodelabs.formflow.modules.forms.application.usecase.response;

import com.kodelabs.formflow.modules.forms.application.service.ResponseDetailAssembler;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetResponseDetailUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetResponseDetailQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.AnswerDetailResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponseCategoryScoreResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponseDetailResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
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

    private final FormLoader formLoader;
    private final FormResponseRepositoryPort responseRepository;
    private final CandidateRepositoryPort candidateRepository;
    private final ResponseDetailAssembler responseDetailAssembler;

    @Override
    @Transactional(readOnly = true)
    public ResponseDetailResult execute(GetResponseDetailQuery query) {
        formLoader.loadOrThrow(query.formId(), query.tenantId());
        FormResponse response = loadResponseInForm(query.responseId(), query.formId(), query.tenantId());
        Candidate candidate = loadCandidate(response.getCandidateId());
        Double totalScore = responseDetailAssembler.resolveScore(candidate);
        List<ResponseCategoryScoreResult> categoryScores =
                responseDetailAssembler.resolveCategoryScores(candidate, query.formId(), query.tenantId());
        List<AnswerDetailResult> answers = responseDetailAssembler.buildOrderedAnswers(response);
        return new ResponseDetailResult(
                response.getId(), response.getFormId(), response.getRespondentToken(),
                response.getConvocatoriaId(), response.getCandidateId(), totalScore, categoryScores,
                response.getFormSnapshot(), answers, response.getSubmittedAt(), response.getStartedAt());
    }

    private FormResponse loadResponseInForm(UUID responseId, UUID formId, UUID tenantId) {
        FormResponse response = responseRepository.findByIdAndTenantId(responseId, tenantId)
                .orElseThrow(() -> new BusinessException("error.response.not_found", HttpStatus.NOT_FOUND, responseId));
        if (!formId.equals(response.getFormId())) {
            throw new BusinessException("error.response.not_found", HttpStatus.NOT_FOUND, responseId);
        }
        return response;
    }

    private Candidate loadCandidate(UUID candidateId) {
        if (candidateId == null) return null;
        return candidateRepository.findAllByIds(List.of(candidateId)).stream().findFirst().orElse(null);
    }
}
