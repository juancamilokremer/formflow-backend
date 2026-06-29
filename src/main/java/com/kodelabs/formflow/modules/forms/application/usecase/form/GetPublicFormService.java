package com.kodelabs.formflow.modules.forms.application.usecase.form;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetPublicFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetPublicFormQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.PublicFormResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.PublicQuestionResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.PublicSectionResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPublicFormService implements GetPublicFormUseCase {

    private final FormRepositoryPort formRepository;
    private final TenantRepositoryPort tenantRepository;

    @Override
    public PublicFormResult execute(GetPublicFormQuery query) {
        Form form = loadActiveForm(query.formId());
        Optional<Tenant> tenant = tenantRepository.findById(form.getTenantId());
        List<PublicSectionResult> sections = mapSections(form.getSections());
        return new PublicFormResult(
                form.getId(), form.getName(), form.getType(), form.getTimeLimitSeconds(),
                tenant.map(Tenant::getName).orElse(null),
                tenant.map(Tenant::getLogoUrl).orElse(null),
                tenant.map(Tenant::getPrimaryColor).orElse(null),
                sections);
    }

    private Form loadActiveForm(UUID formId) {
        Form form = formRepository.findByIdPublicWithSections(formId)
                .orElseThrow(() -> new BusinessException(
                        "error.form.not_found", HttpStatus.NOT_FOUND, formId.toString()));
        if (form.getStatus() != FormStatus.ACTIVE) {
            throw new BusinessException(
                    "error.form.not_found", HttpStatus.NOT_FOUND, formId.toString());
        }
        return form;
    }

    private List<PublicSectionResult> mapSections(List<FormSection> sections) {
        return sections.stream()
                .sorted(Comparator.comparingInt(FormSection::getPosition))
                .map(this::toSectionResult)
                .toList();
    }

    private PublicSectionResult toSectionResult(FormSection section) {
        List<PublicQuestionResult> questions = section.getQuestions().stream()
                .sorted(Comparator.comparingInt(FormQuestion::getPosition))
                .map(this::toQuestionResult)
                .toList();
        return new PublicSectionResult(
                section.getId(), section.getTitle(), section.getDescription(),
                section.getPosition(), section.getTimeLimitSeconds(), questions);
    }

    private PublicQuestionResult toQuestionResult(FormQuestion q) {
        return new PublicQuestionResult(
                q.getId(), q.getTitle(), q.getDescription(), q.getType(),
                q.getPosition(), q.isRequired(), q.getTimeLimitSeconds(),
                q.getConditionalLogic(), q.getConfig());
    }
}
