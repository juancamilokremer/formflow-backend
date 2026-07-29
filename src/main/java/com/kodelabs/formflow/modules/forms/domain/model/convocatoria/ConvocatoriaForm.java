package com.kodelabs.formflow.modules.forms.domain.model.convocatoria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConvocatoriaForm {

    private UUID id;
    private UUID convocatoriaId;
    private UUID formId;

    @Builder.Default
    private int weight = 100;

    @Builder.Default
    private List<CategoryWeight> categoryWeights = new ArrayList<>();

    private Integer minScore;

    @Builder.Default
    private int position = 0;

    private Instant createdAt;
    private Instant updatedAt;
}
