package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.CsvParserService;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ImportCandidatesCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ImportResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportCandidatesServiceTest {

    @Mock private ConvocatoriaRepositoryPort convocatoriaRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @Mock private CsvParserService csvParser;

    @InjectMocks private ImportCandidatesService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId   = UUID.randomUUID();
    private final UUID convId   = UUID.randomUUID();

    @Test
    void importsNewCandidatesAndSkipsDuplicates() {
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId))
                .thenReturn(Optional.of(draftConvocatoria()));
        when(csvParser.parse(any())).thenReturn(List.of(
                new CsvParserService.CsvCandidate("María G.", "maria@test.com"),
                new CsvParserService.CsvCandidate("Carlos R.", "carlos@test.com")));
        when(candidateRepository.existsByConvocatoriaIdAndEmail(convId, "maria@test.com")).thenReturn(false);
        when(candidateRepository.existsByConvocatoriaIdAndEmail(convId, "carlos@test.com")).thenReturn(true);
        when(candidateRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        ImportResult result = service.execute(new ImportCandidatesCommand(
                convId, tenantId, userId, "data".getBytes(StandardCharsets.UTF_8)));

        assertThat(result.imported()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.errors()).hasSize(1);
    }

    @Test
    void throwsConflictWhenConvocatoriaIsClosed() {
        Convocatoria closed = draftConvocatoria();
        closed.launch();
        closed.close();
        when(convocatoriaRepository.findByIdAndTenantId(convId, tenantId)).thenReturn(Optional.of(closed));

        assertThatThrownBy(() -> service.execute(new ImportCandidatesCommand(
                convId, tenantId, userId, new byte[0])))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    private Convocatoria draftConvocatoria() {
        return Convocatoria.builder().id(convId).tenantId(tenantId)
                .formId(UUID.randomUUID()).name("Test").status(ConvocatoriaStatus.DRAFT).build();
    }
}
