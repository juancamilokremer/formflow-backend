package com.kodelabs.formflow.modules.forms.domain.model;

import com.kodelabs.formflow.modules.forms.domain.model.snapshot.FormSnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormResponse {

    private UUID id;
    private UUID formId;
    private UUID tenantId;
    private UUID convocatoriaId;
    private UUID respondentToken;
    private FormSnapshot formSnapshot;
    private Instant submittedAt;
    private Instant createdAt;
}
