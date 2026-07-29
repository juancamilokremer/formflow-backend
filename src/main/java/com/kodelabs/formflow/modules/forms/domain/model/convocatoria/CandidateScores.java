package com.kodelabs.formflow.modules.forms.domain.model.convocatoria;

import java.util.List;

public record CandidateScores(Double total, List<CandidateFormScore> perForm) {}
