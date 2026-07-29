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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "convocatoria_forms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConvocatoriaFormJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "convocatoria_id", nullable = false)
    private UUID convocatoriaId;

    @Column(name = "form_id", nullable = false)
    private UUID formId;

    @Column(name = "weight", nullable = false)
    private int weight;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "category_weights", columnDefinition = "jsonb", nullable = false)
    private String categoryWeights;

    @Column(name = "min_score")
    private Integer minScore;

    @Column(name = "position", nullable = false)
    private int position;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
