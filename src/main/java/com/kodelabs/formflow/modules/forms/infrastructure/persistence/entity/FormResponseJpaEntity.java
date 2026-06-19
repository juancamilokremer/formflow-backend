package com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "form_responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormResponseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "form_id", nullable = false)
    private UUID formId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "convocatoria_id")
    private UUID convocatoriaId;

    @Column(name = "respondent_token", nullable = false, unique = true)
    private UUID respondentToken;

    @Column(name = "form_snapshot", columnDefinition = "jsonb", nullable = false)
    private String formSnapshot;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
