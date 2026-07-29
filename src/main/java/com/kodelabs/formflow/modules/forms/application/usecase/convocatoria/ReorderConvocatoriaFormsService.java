package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.port.in.ReorderConvocatoriaFormsUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ReorderConvocatoriaFormsCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaFormResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaFormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReorderConvocatoriaFormsService implements ReorderConvocatoriaFormsUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final ConvocatoriaFormRepositoryPort convocatoriaFormRepository;

    @Override
    @Transactional
    public List<ConvocatoriaFormResult> execute(ReorderConvocatoriaFormsCommand command) {
        Convocatoria convocatoria = loadDraftConvocatoria(command.convocatoriaId(), command.tenantId());
        List<ConvocatoriaForm> active = convocatoria.getForms();

        Map<UUID, ConvocatoriaForm> byId = active.stream()
                .collect(Collectors.toMap(ConvocatoriaForm::getId, f -> f));

        if (!byId.keySet().containsAll(command.orderedConvocatoriaFormIds())
                || command.orderedConvocatoriaFormIds().size() != active.size()) {
            throw new BusinessException("error.convocatoria.form_reorder_invalid", HttpStatus.BAD_REQUEST);
        }

        List<UUID> ordered = command.orderedConvocatoriaFormIds();
        for (int i = 0; i < ordered.size(); i++) {
            byId.get(ordered.get(i)).setPosition(i);
        }
        List<ConvocatoriaForm> reordered = ordered.stream().map(byId::get).toList();

        return convocatoriaFormRepository.saveAll(reordered).stream()
                .map(ConvocatoriaFormResult::from)
                .toList();
    }

    private Convocatoria loadDraftConvocatoria(UUID id, UUID tenantId) {
        Convocatoria convocatoria = convocatoriaRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new BusinessException("error.convocatoria.not_found", HttpStatus.NOT_FOUND, id));
        if (!convocatoria.isDraft()) {
            throw new BusinessException("error.convocatoria.not_draft", HttpStatus.CONFLICT);
        }
        return convocatoria;
    }
}
