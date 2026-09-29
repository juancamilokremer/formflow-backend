package com.kodelabs.formflow.modules.forms.application.usecase.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.DownloadAnswerFileCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.DownloadAnswerFileResult;
import com.kodelabs.formflow.shared.exception.BusinessException;
import com.kodelabs.formflow.shared.storage.FileStoragePort;
import com.kodelabs.formflow.shared.storage.StoredFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DownloadAnswerFileServiceTest {

    @Mock private FileStoragePort fileStorage;
    @InjectMocks private DownloadAnswerFileService service;

    @Test
    void returnsTheStoredFileWhenItExists() {
        UUID fileId = UUID.randomUUID();
        byte[] content = "contenido".getBytes();
        when(fileStorage.load(fileId)).thenReturn(Optional.of(new StoredFile("cv.pdf", "application/pdf", content)));

        DownloadAnswerFileResult result = service.execute(new DownloadAnswerFileCommand(fileId));

        assertThat(result.filename()).isEqualTo("cv.pdf");
        assertThat(result.contentType()).isEqualTo("application/pdf");
        assertThat(result.content()).isEqualTo(content);
    }

    @Test
    void throwsNotFoundWhenTheFileDoesNotExist() {
        UUID fileId = UUID.randomUUID();
        when(fileStorage.load(fileId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new DownloadAnswerFileCommand(fileId)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
