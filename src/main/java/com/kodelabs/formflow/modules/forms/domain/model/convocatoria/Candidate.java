package com.kodelabs.formflow.modules.forms.domain.model.convocatoria;

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
public class Candidate {

    private UUID id;
    private UUID convocatoriaId;
    private UUID tenantId;
    private String name;
    private String email;

    @Builder.Default
    private UUID token = UUID.randomUUID();

    @Builder.Default
    private CandidateStatus status = CandidateStatus.INVITED;

    private UUID responseId;
    private CandidateScores scores;
    private Instant invitedAt;
    private Instant respondedAt;
    private Instant createdAt;
}
