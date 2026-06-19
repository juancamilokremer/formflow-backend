package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.CsvParserService;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.port.in.ImportCandidatesUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ImportCandidatesCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ImportResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportCandidatesService implements ImportCandidatesUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final CandidateRepositoryPort candidateRepository;
    private final CsvParserService csvParser;

    @Override
    @Transactional
    public ImportResult execute(ImportCandidatesCommand command) {
        Convocatoria convocatoria = loadOpenConvocatoria(command);
        List<CsvParserService.CsvCandidate> rows = csvParser.parse(command.csvContent());
        return importRows(rows, convocatoria);
    }

    private Convocatoria loadOpenConvocatoria(ImportCandidatesCommand command) {
        Convocatoria convocatoria = convocatoriaRepository
                .findByIdAndTenantId(command.convocatoriaId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.convocatoria.not_found",
                        HttpStatus.NOT_FOUND, command.convocatoriaId()));
        if (convocatoria.isClosed()) {
            throw new BusinessException("error.convocatoria.already_closed", HttpStatus.CONFLICT);
        }
        return convocatoria;
    }

    private ImportResult importRows(List<CsvParserService.CsvCandidate> rows, Convocatoria convocatoria) {
        List<Candidate> toSave = new ArrayList<>();
        List<String> errors   = new ArrayList<>();
        int skipped = 0;
        for (CsvParserService.CsvCandidate row : rows) {
            if (candidateRepository.existsByConvocatoriaIdAndEmail(convocatoria.getId(), row.email())) {
                skipped++;
                errors.add("Email ya existe: " + row.email());
            } else {
                toSave.add(buildCandidate(row, convocatoria));
            }
        }
        if (!toSave.isEmpty()) candidateRepository.saveAll(toSave);
        return new ImportResult(toSave.size(), skipped, errors);
    }

    private Candidate buildCandidate(CsvParserService.CsvCandidate row, Convocatoria convocatoria) {
        return Candidate.builder()
                .convocatoriaId(convocatoria.getId())
                .tenantId(convocatoria.getTenantId())
                .name(row.name())
                .email(row.email())
                .invitedAt(Instant.now())
                .build();
    }
}
