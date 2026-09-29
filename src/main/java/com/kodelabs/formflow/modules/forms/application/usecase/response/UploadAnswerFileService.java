package com.kodelabs.formflow.modules.forms.application.usecase.response;

import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.FileConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.UploadAnswerFileUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UploadAnswerFileCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.UploadAnswerFileResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FileStoragePort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadAnswerFileService implements UploadAnswerFileUseCase {

    private final FormLoader formLoader;
    private final FileStoragePort fileStorage;

    @Override
    public UploadAnswerFileResult execute(UploadAnswerFileCommand command) {
        Form form = formLoader.loadPublicOrThrow(command.formId());
        FormQuestion question = findQuestion(form, command.questionId());
        FileConfig config = requireFileConfig(question);

        validateSize(command.content().length, config.getMaxSizeMb());
        String filename = sanitizeFilename(command.originalFilename());
        validateExtension(filename, config.getAllowedTypes());

        UUID fileId = UUID.randomUUID();
        fileStorage.store(fileId, filename, command.content());
        return new UploadAnswerFileResult(fileId, filename);
    }

    private FormQuestion findQuestion(Form form, UUID questionId) {
        return form.getSections().stream()
                .flatMap(section -> section.getQuestions().stream())
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "error.question.not_found", HttpStatus.NOT_FOUND, questionId));
    }

    private FileConfig requireFileConfig(FormQuestion question) {
        if (question.getType() != QuestionType.FILE) {
            throw new BusinessException(
                    "error.question.not_a_file_question", HttpStatus.BAD_REQUEST, question.getId());
        }
        return (FileConfig) question.getConfig();
    }

    private void validateSize(int contentLength, int maxSizeMb) {
        long maxBytes = maxSizeMb * 1024L * 1024L;
        if (contentLength > maxBytes) {
            throw new BusinessException("error.file.too_large", HttpStatus.BAD_REQUEST, maxSizeMb);
        }
    }

    private void validateExtension(String filename, List<String> allowedTypes) {
        String extension = extensionOf(filename);
        boolean allowed = allowedTypes != null && allowedTypes.stream()
                .anyMatch(t -> t.equalsIgnoreCase(extension));
        if (!allowed) {
            throw new BusinessException(
                    "error.file.type_not_allowed", HttpStatus.BAD_REQUEST, extension, allowedTypes);
        }
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
    }

    /** Keeps only the leaf filename — strips any path segments a malicious client could send
     *  (e.g. "../../etc/passwd") since this is used as-is as the stored file's name. */
    private String sanitizeFilename(String originalFilename) {
        String leaf = originalFilename.replace('\\', '/');
        int lastSlash = leaf.lastIndexOf('/');
        leaf = lastSlash >= 0 ? leaf.substring(lastSlash + 1) : leaf;
        return leaf.isBlank() ? "archivo" : leaf;
    }
}
