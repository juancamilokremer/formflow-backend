package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateClassification;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateScores;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateStatus;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetRankingQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.RankingEntryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetRankingServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @Mock private CategoryRepositoryPort categoryRepository;
    @InjectMocks private GetRankingService service;

    private UUID convId;
    private UUID tenantId;
    private UUID catId;
    private Convocatoria convocatoria;

    @BeforeEach
    void setUp() {
        convId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        catId = UUID.randomUUID();

        convocatoria = Convocatoria.builder()
                .id(convId).tenantId(tenantId).name("Dev 2026")
                .categoryWeights(List.of(new CategoryWeight(catId, 100)))
                .scoringConfig(new ScoringConfig(70, 50))
                .build();
    }

    @Test
    void ranksCandidatesByScoreDescAndClassifiesCorrectly() {
        Candidate maria  = candidateWithScore("María",  88.0);
        Candidate carlos = candidateWithScore("Carlos", 62.0);
        Candidate pedro  = candidateWithScore("Pedro",  40.0);

        stubConvocatoria();
        when(candidateRepository.findAllByConvocatoriaId(convId))
                .thenReturn(List.of(carlos, pedro, maria));
        stubCategories();

        List<RankingEntryResult> result = service.execute(new GetRankingQuery(convId, tenantId));

        assertThat(result).hasSize(3);
        assertThat(result.get(0).name()).isEqualTo("María");
        assertThat(result.get(0).rank()).isEqualTo(1);
        assertThat(result.get(0).classification()).isEqualTo(CandidateClassification.APTO);

        assertThat(result.get(1).name()).isEqualTo("Carlos");
        assertThat(result.get(1).rank()).isEqualTo(2);
        assertThat(result.get(1).classification()).isEqualTo(CandidateClassification.REVISAR);

        assertThat(result.get(2).name()).isEqualTo("Pedro");
        assertThat(result.get(2).rank()).isEqualTo(3);
        assertThat(result.get(2).classification()).isEqualTo(CandidateClassification.NO_APTO);
    }

    @Test
    void candidatesWithoutResponseAppearAtEndWithNullRank() {
        Candidate responded = candidateWithScore("Ana", 75.0);
        Candidate pending   = candidateWithoutScore("Luis");

        stubConvocatoria();
        when(candidateRepository.findAllByConvocatoriaId(convId))
                .thenReturn(List.of(pending, responded));
        stubCategories();

        List<RankingEntryResult> result = service.execute(new GetRankingQuery(convId, tenantId));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Ana");
        assertThat(result.get(0).rank()).isEqualTo(1);
        assertThat(result.get(1).name()).isEqualTo("Luis");
        assertThat(result.get(1).rank()).isNull();
        assertThat(result.get(1).classification()).isNull();
    }

    @Test
    void scoresByCategoryUseCategoryName() {
        stubConvocatoria();
        when(candidateRepository.findAllByConvocatoriaId(convId))
                .thenReturn(List.of(candidateWithScore("Ana", 80.0)));
        when(categoryRepository.findAllByIdsAndTenantId(anyList(), eq(tenantId)))
                .thenReturn(List.of(Category.builder().id(catId).name("Habilidades Blandas").build()));

        List<RankingEntryResult> result = service.execute(new GetRankingQuery(convId, tenantId));

        assertThat(result.get(0).scoresByCategory()).containsKey("Habilidades Blandas");
        assertThat(result.get(0).scoresByCategory().get("Habilidades Blandas")).isEqualTo(80.0);
    }

    @Test
    void throwsNotFoundWhenConvocatoriaDoesNotBelongToTenant() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new GetRankingQuery(convId, tenantId)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    private void stubConvocatoria() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId))
                .thenReturn(Optional.of(convocatoria));
    }

    private void stubCategories() {
        when(categoryRepository.findAllByIdsAndTenantId(anyList(), eq(tenantId)))
                .thenReturn(List.of(Category.builder().id(catId).name("Técnicas").build()));
    }

    private Candidate candidateWithScore(String name, double totalScore) {
        return Candidate.builder()
                .id(UUID.randomUUID()).convocatoriaId(convId).tenantId(tenantId)
                .name(name).email(name.toLowerCase() + "@test.com")
                .status(CandidateStatus.RESPONDED)
                .scores(new CandidateScores(totalScore, Map.of(catId, totalScore)))
                .build();
    }

    private Candidate candidateWithoutScore(String name) {
        return Candidate.builder()
                .id(UUID.randomUUID()).convocatoriaId(convId).tenantId(tenantId)
                .name(name).email(name.toLowerCase() + "@test.com")
                .status(CandidateStatus.INVITED)
                .build();
    }
}
