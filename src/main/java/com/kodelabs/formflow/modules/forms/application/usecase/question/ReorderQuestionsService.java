package com.kodelabs.formflow.modules.forms.application.usecase.question;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.port.in.ReorderQuestionsUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ReorderQuestionsCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
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
public class ReorderQuestionsService implements ReorderQuestionsUseCase {

    private final FormQuestionRepositoryPort questionRepository;
    private final FormRepositoryPort formRepository;

    @Override
    @Transactional
    public List<QuestionResult> execute(ReorderQuestionsCommand command) {
        Form form = formRepository.findByIdAndTenantId(command.formId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.form.not_found",
                        HttpStatus.NOT_FOUND, command.formId().toString()));

        List<FormQuestion> active = questionRepository
                .findActiveBySectionIdAndTenantId(command.sectionId(), command.tenantId());

        Map<UUID, FormQuestion> byId = active.stream()
                .collect(Collectors.toMap(FormQuestion::getId, q -> q));

        if (!byId.keySet().containsAll(command.orderedQuestionIds())
                || command.orderedQuestionIds().size() != active.size()) {
            throw new BusinessException("error.question.reorder_invalid", HttpStatus.BAD_REQUEST);
        }

        List<UUID> ordered = command.orderedQuestionIds();
        for (int i = 0; i < ordered.size(); i++) {
            byId.get(ordered.get(i)).setPosition(i);
        }
        List<FormQuestion> reordered = ordered.stream().map(byId::get).toList();
        questionRepository.saveAll(reordered);

        form.incrementVersion();
        form.setUpdatedBy(command.userId());
        formRepository.save(form);

        return reordered.stream().map(QuestionResult::from).toList();
    }
}
