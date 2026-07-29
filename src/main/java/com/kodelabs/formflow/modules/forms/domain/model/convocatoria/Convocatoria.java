package com.kodelabs.formflow.modules.forms.domain.model.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.FormType;
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
public class Convocatoria {

    private UUID id;
    private UUID tenantId;
    private String name;
    private FormType type;

    @Builder.Default
    private ConvocatoriaStatus status = ConvocatoriaStatus.DRAFT;

    @Builder.Default
    private List<ConvocatoriaForm> forms = new ArrayList<>();

    @Builder.Default
    private ScoringConfig scoringConfig = ScoringConfig.defaults();

    private Instant startDate;
    private Instant endDate;
    private Instant deletedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getFormId() {
        return forms.isEmpty() ? null : forms.get(0).getFormId();
    }

    public List<CategoryWeight> getCategoryWeights() {
        return forms.isEmpty() ? List.of() : forms.get(0).getCategoryWeights();
    }

    public boolean isDraft()   { return status == ConvocatoriaStatus.DRAFT; }
    public boolean isActive()  { return status == ConvocatoriaStatus.ACTIVE; }
    public boolean isClosed()  { return status == ConvocatoriaStatus.CLOSED; }
    public boolean isDeleted() { return deletedAt != null; }

    public void launch() {
        this.status = ConvocatoriaStatus.ACTIVE;
        this.startDate = Instant.now();
    }

    public void close() {
        this.status = ConvocatoriaStatus.CLOSED;
        this.endDate = Instant.now();
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }
}
