package com.kodelabs.formflow.modules.forms.application.usecase.section;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.port.in.ReorderSectionsUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ReorderSectionsCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SectionResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
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
public class ReorderSectionsService implements ReorderSectionsUseCase {

    private final FormRepositoryPort formRepository;
    private final FormSectionRepositoryPort sectionRepository;

    @Override
    @Transactional
    public List<SectionResult> execute(ReorderSectionsCommand command) {
        Form form = formRepository
                .findByIdAndTenantId(command.formId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND,
                        command.formId().toString()));

        List<FormSection> active = sectionRepository.findActiveByFormIdAndTenantId(
                command.formId(), command.tenantId());

        Map<UUID, FormSection> byId = active.stream()
                .collect(Collectors.toMap(FormSection::getId, s -> s));

        if (!byId.keySet().containsAll(command.orderedSectionIds())
                || command.orderedSectionIds().size() != active.size()) {
            throw new BusinessException("error.section.reorder_invalid", HttpStatus.BAD_REQUEST);
        }

        List<UUID> ordered = command.orderedSectionIds();
        for (int i = 0; i < ordered.size(); i++) {
            byId.get(ordered.get(i)).setPosition(i);
        }
        List<FormSection> reordered = ordered.stream().map(byId::get).toList();

        sectionRepository.saveAll(reordered);

        form.incrementVersion();
        form.setUpdatedBy(command.userId());
        formRepository.save(form);

        return reordered.stream().map(SectionResult::from).toList();
    }
}
