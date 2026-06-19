package com.kodelabs.formflow.modules.forms.domain.model.convocatoria;

public record ScoringConfig(int aptoMin, int revisarMin) {

    public static ScoringConfig defaults() {
        return new ScoringConfig(70, 50);
    }
}
