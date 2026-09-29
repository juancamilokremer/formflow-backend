package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.FileConfig;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Full round trip through PublicFileController: upload then download, real HTTP, real
 *  filesystem (a temp dir, see application-test.yml's app.uploads.dir override). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PublicFileControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FormRepositoryPort formRepository;

    @Autowired
    private FormSectionRepositoryPort sectionRepository;

    @Autowired
    private FormQuestionRepositoryPort questionRepository;

    @Test
    void uploadingThenDownloadingReturnsTheExactSameBytes() {
        UUID tenantId = UUID.randomUUID();
        Form form = formRepository.save(Form.builder()
                .tenantId(tenantId).name("Encuesta").type(FormType.REGISTRATION).status(FormStatus.ACTIVE).version(1)
                .build());
        FormSection section = sectionRepository.save(FormSection.builder()
                .formId(form.getId()).tenantId(tenantId).title("Sección").position(0).build());
        FormQuestion question = questionRepository.save(FormQuestion.builder()
                .formId(form.getId()).sectionId(section.getId()).tenantId(tenantId)
                .title("Sube tu CV").type(QuestionType.FILE).position(0).required(false)
                .config(FileConfig.builder().maxSizeMb(5).allowedTypes(List.of("pdf")).build())
                .build());

        byte[] fileBytes = "contenido de prueba".getBytes();
        String uploadUrl = "http://localhost:" + port
                + "/api/v1/public/forms/" + form.getId() + "/questions/" + question.getId() + "/files";

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() { return "cv.pdf"; }
        });
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<UploadResponse> uploadResponse = restTemplate.postForEntity(
                uploadUrl, new HttpEntity<>(body, headers), UploadResponse.class);

        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String downloadUrl = uploadResponse.getBody().data().url();
        assertThat(downloadUrl).contains("/api/v1/public/files/");

        ResponseEntity<byte[]> downloadResponse = restTemplate.getForEntity(downloadUrl, byte[].class);

        assertThat(downloadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(downloadResponse.getBody()).isEqualTo(fileBytes);
        assertThat(downloadResponse.getHeaders().getContentDisposition().getFilename()).isEqualTo("cv.pdf");
    }

    @Test
    void rejectsAFileTypeNotAllowedByTheQuestion() {
        UUID tenantId = UUID.randomUUID();
        Form form = formRepository.save(Form.builder()
                .tenantId(tenantId).name("Encuesta").type(FormType.REGISTRATION).status(FormStatus.ACTIVE).version(1)
                .build());
        FormSection section = sectionRepository.save(FormSection.builder()
                .formId(form.getId()).tenantId(tenantId).title("Sección").position(0).build());
        FormQuestion question = questionRepository.save(FormQuestion.builder()
                .formId(form.getId()).sectionId(section.getId()).tenantId(tenantId)
                .title("Sube tu CV").type(QuestionType.FILE).position(0).required(false)
                .config(FileConfig.builder().maxSizeMb(5).allowedTypes(List.of("pdf")).build())
                .build());

        String uploadUrl = "http://localhost:" + port
                + "/api/v1/public/forms/" + form.getId() + "/questions/" + question.getId() + "/files";

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource("x".getBytes()) {
            @Override
            public String getFilename() { return "malware.exe"; }
        });
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<String> response = restTemplate.postForEntity(
                uploadUrl, new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private record UploadResponse(UploadData data) {}
    private record UploadData(String url) {}
}
