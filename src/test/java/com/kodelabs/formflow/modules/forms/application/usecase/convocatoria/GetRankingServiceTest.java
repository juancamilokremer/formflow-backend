package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateScores;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetRankingQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetRankingServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private CandidateRepositoryPort candidateRepository;

    @InjectMocks private GetRankingService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID convId   = UUID.randomUUID();

    @Test
    void returnsCandidatesSortedByScoreDescending() {
        when(convocatoriaRepository.existsByIdAndTenantId(convId, tenantId)).thenReturn(true);
        when(candidateRepository.findAllByConvocatoriaId(convId)).thenReturn(List.of(
                candidateWithScore("Carlos", 65.0),
                candidateWithScore("María", 88.0),
                candidateWithScore("Pedro", 72.0)));

        List<CandidateResult> ranking = service.execute(new GetRankingQuery(convId, tenantId));

        assertThat(ranking).hasSize(3);
        assertThat(ranking.get(0).name()).isEqualTo("María");
        assertThat(ranking.get(1).name()).isEqualTo("Pedro");
        assertThat(ranking.get(2).name()).isEqualTo("Carlos");
    }

    @Test
    void excludesCandidatesWithoutScore() {
        when(convocatoriaRepository.existsByIdAndTenantId(convId, tenantId)).thenReturn(true);
        when(candidateRepository.findAllByConvocatoriaId(convId)).thenReturn(List.of(
                candidateWithScore("Respondió", 80.0),
                candidateWithoutScore("Pendiente")));

        List<CandidateResult> ranking = service.execute(new GetRankingQuery(convId, tenantId));

        assertThat(ranking).hasSize(1);
        assertThat(ranking.get(0).name()).isEqualTo("Respondió");
    }

    @Test
    void throwsNotFoundWhenConvocatoriaDoesNotExist() {
        when(convocatoriaRepository.existsByIdAndTenantId(convId, tenantId)).thenReturn(false);

        assertThatThrownBy(() -> service.execute(new GetRankingQuery(convId, tenantId)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private Candidate candidateWithScore(String name, double total) {
        return Candidate.builder()
                .id(UUID.randomUUID()).convocatoriaId(convId).tenantId(tenantId)
                .name(name).email(name.toLowerCase() + "@test.com")
                .status(CandidateStatus.RESPONDED).token(UUID.randomUUID())
                .scores(new CandidateScores(total, Map.of()))
                .build();
    }

    private Candidate candidateWithoutScore(String name) {
        return Candidate.builder()
                .id(UUID.randomUUID()).convocatoriaId(convId).tenantId(tenantId)
                .name(name).email(name.toLowerCase() + "@test.com")
                .status(CandidateStatus.INVITED).token(UUID.randomUUID())
                .build();
    }
}
