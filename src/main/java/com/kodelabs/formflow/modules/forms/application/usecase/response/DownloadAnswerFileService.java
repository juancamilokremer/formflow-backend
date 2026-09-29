package com.kodelabs.formflow.modules.forms.application.usecase.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.DownloadAnswerFileUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DownloadAnswerFileCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.DownloadAnswerFileResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FileStoragePort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DownloadAnswerFileService implements DownloadAnswerFileUseCase {

    private final FileStoragePort fileStorage;

    @Override
    public DownloadAnswerFileResult execute(DownloadAnswerFileCommand command) {
        var file = fileStorage.load(command.fileId())
                .orElseThrow(() -> new BusinessException("error.file.not_found", HttpStatus.NOT_FOUND));
        return new DownloadAnswerFileResult(file.filename(), file.contentType(), file.content());
    }
}
