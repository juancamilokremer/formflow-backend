package com.kodelabs.formflow.modules.forms.application.usecase.response;

import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.FileConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.TextConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UploadAnswerFileCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.UploadAnswerFileResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FileStoragePort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadAnswerFileServiceTest {

    @Mock private FormLoader formLoader;
    @Mock private FileStoragePort fileStorage;
    @InjectMocks private UploadAnswerFileService service;

    private final UUID formId = UUID.randomUUID();
    private final UUID questionId = UUID.randomUUID();

    private Form formWithFileQuestion(FileConfig config) {
        FormQuestion question = FormQuestion.builder()
                .id(questionId).type(QuestionType.FILE).title("Sube tu CV").config(config).build();
        FormSection section = FormSection.builder().id(UUID.randomUUID()).questions(List.of(question)).build();
        return Form.builder().id(formId).sections(List.of(section)).build();
    }

    @BeforeEach
    void setUp() {
        when(formLoader.loadPublicOrThrow(formId)).thenReturn(
                formWithFileQuestion(FileConfig.builder().maxSizeMb(5).allowedTypes(List.of("pdf", "jpg")).build()));
    }

    @Test
    void storesTheFileAndReturnsItsId() {
        byte[] content = "contenido".getBytes();
        var command = new UploadAnswerFileCommand(formId, questionId, "cv.pdf", content);

        UploadAnswerFileResult result = service.execute(command);

        assertThat(result.filename()).isEqualTo("cv.pdf");
        ArgumentCaptor<UUID> fileIdCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(fileStorage).store(fileIdCaptor.capture(), eq("cv.pdf"), eq(content));
        assertThat(result.fileId()).isEqualTo(fileIdCaptor.getValue());
    }

    @Test
    void stripsPathSegmentsFromTheOriginalFilename() {
        var command = new UploadAnswerFileCommand(formId, questionId, "../../etc/passwd.pdf", "x".getBytes());

        UploadAnswerFileResult result = service.execute(command);

        assertThat(result.filename()).isEqualTo("passwd.pdf");
        verify(fileStorage).store(any(), eq("passwd.pdf"), any());
    }

    @Test
    void rejectsAFileLargerThanTheConfiguredMax() {
        byte[] tooLarge = new byte[6 * 1024 * 1024]; // maxSizeMb is 5
        var command = new UploadAnswerFileCommand(formId, questionId, "big.pdf", tooLarge);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void rejectsAnExtensionNotInAllowedTypes() {
        var command = new UploadAnswerFileCommand(formId, questionId, "malware.exe", "x".getBytes());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.file.type_not_allowed");
    }

    @Test
    void rejectsWhenTheQuestionIsNotOfTypeFile() {
        FormQuestion textQuestion = FormQuestion.builder()
                .id(questionId).type(QuestionType.TEXT).title("Cuentanos").config(new TextConfig()).build();
        Form form = Form.builder().id(formId)
                .sections(List.of(FormSection.builder().id(UUID.randomUUID()).questions(List.of(textQuestion)).build()))
                .build();
        when(formLoader.loadPublicOrThrow(formId)).thenReturn(form);

        var command = new UploadAnswerFileCommand(formId, questionId, "cv.pdf", "x".getBytes());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.question.not_a_file_question");
    }

    @Test
    void rejectsWhenTheQuestionDoesNotExistInTheForm() {
        var command = new UploadAnswerFileCommand(formId, UUID.randomUUID(), "cv.pdf", "x".getBytes());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
