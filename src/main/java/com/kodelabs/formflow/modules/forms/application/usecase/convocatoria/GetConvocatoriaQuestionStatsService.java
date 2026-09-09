package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.QuestionStatsComputer;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetConvocatoriaQuestionStatsUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetConvocatoriaQuestionStatsQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaQuestionStatsResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormQuestionStatsResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
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
public class GetConvocatoriaQuestionStatsService implements GetConvocatoriaQuestionStatsUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final FormRepositoryPort formRepository;
    private final FormResponseRepositoryPort responseRepository;
    private final QuestionStatsComputer questionStatsComputer;

    @Override
    @Transactional(readOnly = true)
    public ConvocatoriaQuestionStatsResult execute(GetConvocatoriaQuestionStatsQuery query) {
        Convocatoria convocatoria = loadConvocatoria(query);
        List<FormResponse> responses = responseRepository.findAllByConvocatoriaIdAndTenantId(
                query.convocatoriaId(), query.tenantId(), query.submittedAtFrom(), query.submittedAtTo());
        Map<UUID, List<FormResponse>> responsesByFormId =
                responses.stream().collect(Collectors.groupingBy(FormResponse::getFormId));

        List<FormQuestionStatsResult> forms = convocatoria.getForms().stream()
                .map(convocatoriaForm -> computeFormStats(convocatoriaForm, responsesByFormId, query.tenantId()))
                .toList();

        return new ConvocatoriaQuestionStatsResult(convocatoria.getId(), convocatoria.getName(), forms);
    }

    private FormQuestionStatsResult computeFormStats(
            ConvocatoriaForm convocatoriaForm,
            Map<UUID, List<FormResponse>> responsesByFormId,
            UUID tenantId) {
        Form form = loadFormWithQuestions(convocatoriaForm.getFormId(), tenantId);
        List<FormResponse> formResponses = responsesByFormId.getOrDefault(form.getId(), List.of());
        var questionStats = questionStatsComputer.compute(form, formResponses.size(), formResponses);
        return new FormQuestionStatsResult(form.getId(), form.getName(), formResponses.size(), questionStats);
    }

    private Convocatoria loadConvocatoria(GetConvocatoriaQuestionStatsQuery query) {
        return convocatoriaRepository.findByIdAndTenantId(query.convocatoriaId(), query.tenantId())
                .orElseThrow(() -> new BusinessException(
                        "error.convocatoria.not_found", HttpStatus.NOT_FOUND, query.convocatoriaId()));
    }

    private Form loadFormWithQuestions(UUID formId, UUID tenantId) {
        return formRepository.findByIdAndTenantIdWithSections(formId, tenantId)
                .orElseThrow(() -> new BusinessException(
                        "error.form.not_found", HttpStatus.NOT_FOUND, formId));
    }
}
