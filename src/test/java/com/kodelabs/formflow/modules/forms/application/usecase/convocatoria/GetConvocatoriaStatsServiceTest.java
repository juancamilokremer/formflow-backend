package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.CandidateClassifier;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateFormScore;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateScores;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetConvocatoriaStatsQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaStatsResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetConvocatoriaStatsServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @Spy  private CandidateClassifier candidateClassifier = new CandidateClassifier();
    @InjectMocks private GetConvocatoriaStatsService service;

    private final UUID convId = UUID.randomUUID();
    private final UUID tenantId = UUID.randomUUID();
    private final UUID convocatoriaFormId = UUID.randomUUID();
    private final UUID formId = UUID.randomUUID();

    @Test
    void classifiesCandidatesIntoTheThreeParticipationBuckets() {
        Convocatoria convocatoria = draftConvocatoria();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(convocatoria));

        Candidate notStarted = candidateWithStatus(CandidateStatus.INVITED, null);
        Candidate inProgress = candidateWithStatus(CandidateStatus.IN_PROGRESS,
                new CandidateScores(null, List.of(new CandidateFormScore(convocatoriaFormId, formId, 80.0, Map.of()))));
        Candidate responded = candidateWithStatus(CandidateStatus.RESPONDED,
                new CandidateScores(90.0, List.of(new CandidateFormScore(convocatoriaFormId, formId, 90.0, Map.of()))));
        when(candidateRepository.findAllByConvocatoriaId(convId))
                .thenReturn(List.of(notStarted, inProgress, responded));

        ConvocatoriaStatsResult result = service.execute(new GetConvocatoriaStatsQuery(convId, tenantId));

        assertThat(result.total()).isEqualTo(3);
        assertThat(result.notStarted()).isEqualTo(1);
        assertThat(result.inProgress()).isEqualTo(1);
        assertThat(result.responded()).isEqualTo(1);
        assertThat(result.total()).isEqualTo(result.notStarted() + result.inProgress() + result.responded());
    }

    @Test
    void classificationCountsOnlyConsiderRespondedCandidates() {
        Convocatoria convocatoria = draftConvocatoria();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(convocatoria));

        Candidate apto = candidateWithStatus(CandidateStatus.RESPONDED,
                new CandidateScores(85.0, List.of(new CandidateFormScore(convocatoriaFormId, formId, 85.0, Map.of()))));
        Candidate inProgress = candidateWithStatus(CandidateStatus.IN_PROGRESS,
                new CandidateScores(null, List.of(new CandidateFormScore(convocatoriaFormId, formId, 95.0, Map.of()))));
        when(candidateRepository.findAllByConvocatoriaId(convId)).thenReturn(List.of(apto, inProgress));

        ConvocatoriaStatsResult result = service.execute(new GetConvocatoriaStatsQuery(convId, tenantId));

        assertThat(result.aptoCount()).isEqualTo(1);
        assertThat(result.revisarCount()).isZero();
        assertThat(result.noAptoCount()).isZero();
    }

    @Test
    void participationPctIsBasedOnRespondedOverTotal() {
        Convocatoria convocatoria = draftConvocatoria();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(convocatoria));

        Candidate responded = candidateWithStatus(CandidateStatus.RESPONDED,
                new CandidateScores(90.0, List.of(new CandidateFormScore(convocatoriaFormId, formId, 90.0, Map.of()))));
        Candidate notStarted = candidateWithStatus(CandidateStatus.INVITED, null);
        when(candidateRepository.findAllByConvocatoriaId(convId)).thenReturn(List.of(responded, notStarted));

        ConvocatoriaStatsResult result = service.execute(new GetConvocatoriaStatsQuery(convId, tenantId));

        assertThat(result.participationPct()).isEqualTo(50.0);
    }

    @Test
    void throwsNotFoundWhenConvocatoriaDoesNotBelongToTenant() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new GetConvocatoriaStatsQuery(convId, tenantId)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private Convocatoria draftConvocatoria() {
        return Convocatoria.builder()
                .id(convId).tenantId(tenantId).name("Dev 2026")
                .forms(List.of(ConvocatoriaForm.builder().id(convocatoriaFormId).formId(formId).weight(100).build()))
                .status(ConvocatoriaStatus.ACTIVE)
                .scoringConfig(new ScoringConfig(70, 50))
                .build();
    }

    private Candidate candidateWithStatus(CandidateStatus status, CandidateScores scores) {
        return Candidate.builder()
                .id(UUID.randomUUID()).convocatoriaId(convId).tenantId(tenantId)
                .name("Candidato").email("c" + UUID.randomUUID() + "@test.com")
                .status(status).scores(scores)
                .build();
    }
}
