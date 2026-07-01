package com.kodelabs.formflow.modules.forms.application.usecase.response;

import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetResponsesUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetResponsesQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponsePageResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponseSummaryResult;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetResponsesService implements GetResponsesUseCase {

    private final FormLoader formLoader;
    private final FormResponseRepositoryPort responseRepository;
    private final CandidateRepositoryPort candidateRepository;

    @Override
    @Transactional(readOnly = true)
    public ResponsePageResult execute(GetResponsesQuery query) {
        formLoader.loadOrThrow(query.formId(), query.tenantId());
        long total = responseRepository.countByFormIdAndTenantId(query.formId(), query.tenantId());
        List<FormResponse> responses = responseRepository.findPageByFormIdAndTenantId(
                query.formId(), query.tenantId(), query.page(), query.size());
        Map<UUID, Double> scoreByCandidate = loadScoresByCandidate(responses);
        List<ResponseSummaryResult> items = responses.stream()
                .map(r -> toSummary(r, scoreByCandidate))
                .toList();
        int totalPages = query.size() > 0 ? (int) Math.ceil((double) total / query.size()) : 0;
        return new ResponsePageResult(items, total, totalPages, query.page(), query.size());
    }

    private Map<UUID, Double> loadScoresByCandidate(List<FormResponse> responses) {
        List<UUID> candidateIds = responses.stream()
                .map(FormResponse::getCandidateId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (candidateIds.isEmpty()) return Map.of();
        return candidateRepository.findAllByIds(candidateIds).stream()
                .filter(c -> c.getScores() != null)
                .collect(Collectors.toMap(Candidate::getId, c -> c.getScores().total()));
    }

    private ResponseSummaryResult toSummary(FormResponse r, Map<UUID, Double> scoreByCandidate) {
        Double totalScore = r.getCandidateId() != null ? scoreByCandidate.get(r.getCandidateId()) : null;
        return new ResponseSummaryResult(
                r.getId(), r.getRespondentToken(), r.getConvocatoriaId(), r.getCandidateId(),
                totalScore, r.getSubmittedAt(), r.getStartedAt());
    }
}
