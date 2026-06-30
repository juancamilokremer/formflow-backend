package com.kodelabs.formflow.modules.forms.application.usecase.response;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateScores;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetResponsesQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponsePageResult;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetResponsesServiceTest {

    @Mock private FormLoader formLoader;
    @Mock private FormResponseRepositoryPort responseRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @InjectMocks private GetResponsesService service;

    private UUID formId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        formId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
    }

    @Test
    void happyPath_returnsPageWithPaginationMetadata() {
        Form form = Form.builder().id(formId).tenantId(tenantId).build();
        FormResponse r1 = responseWithoutCandidate();
        FormResponse r2 = responseWithoutCandidate();

        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(responseRepository.countByFormIdAndTenantId(formId, tenantId)).thenReturn(25L);
        when(responseRepository.findPageByFormIdAndTenantId(formId, tenantId, 0, 20))
                .thenReturn(List.of(r1, r2));

        ResponsePageResult result = service.execute(new GetResponsesQuery(formId, tenantId, 0, 20));

        assertThat(result.items()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(25);
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(20);
    }

    @Test
    void candidateResponses_includeScoreFromBatchLoadedCandidates() {
        UUID candidateId = UUID.randomUUID();
        Form form = Form.builder().id(formId).tenantId(tenantId).build();
        FormResponse response = FormResponse.builder()
                .id(UUID.randomUUID()).formId(formId).tenantId(tenantId)
                .respondentToken(UUID.randomUUID()).candidateId(candidateId)
                .submittedAt(Instant.now()).build();
        Candidate candidate = Candidate.builder()
                .id(candidateId)
                .scores(new CandidateScores(75.0, Map.of()))
                .build();

        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(responseRepository.countByFormIdAndTenantId(formId, tenantId)).thenReturn(1L);
        when(responseRepository.findPageByFormIdAndTenantId(formId, tenantId, 0, 20))
                .thenReturn(List.of(response));
        when(candidateRepository.findAllByIds(List.of(candidateId))).thenReturn(List.of(candidate));

        ResponsePageResult result = service.execute(new GetResponsesQuery(formId, tenantId, 0, 20));

        assertThat(result.items().get(0).totalScore()).isEqualTo(75.0);
    }

    @Test
    void anonymousResponses_haveNullTotalScore() {
        Form form = Form.builder().id(formId).tenantId(tenantId).build();
        FormResponse response = responseWithoutCandidate();

        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(responseRepository.countByFormIdAndTenantId(formId, tenantId)).thenReturn(1L);
        when(responseRepository.findPageByFormIdAndTenantId(any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(response));

        ResponsePageResult result = service.execute(new GetResponsesQuery(formId, tenantId, 0, 20));

        assertThat(result.items().get(0).totalScore()).isNull();
    }

    @Test
    void formNotFound_throwsNotFound() {
        when(formLoader.loadOrThrow(formId, tenantId)).thenThrow(new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND, formId));

        assertThatThrownBy(() -> service.execute(new GetResponsesQuery(formId, tenantId, 0, 20)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private FormResponse responseWithoutCandidate() {
        return FormResponse.builder()
                .id(UUID.randomUUID()).formId(formId).tenantId(tenantId)
                .respondentToken(UUID.randomUUID())
                .submittedAt(Instant.now())
                .build();
    }
}
