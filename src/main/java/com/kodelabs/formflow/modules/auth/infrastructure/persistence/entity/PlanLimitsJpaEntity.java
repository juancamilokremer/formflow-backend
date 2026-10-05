package com.kodelabs.formflow.modules.auth.infrastructure.persistence.entity;

import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "plan_limits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanLimitsJpaEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "plan", length = 20)
    private TenantPlan plan;

    @Column(name = "forms_limit", nullable = false)
    private int formsLimit;

    @Column(name = "responses_limit", nullable = false)
    private int responsesLimit;

    @Column(name = "users_limit", nullable = false)
    private int usersLimit;

    @Column(name = "convocatorias_limit", nullable = false)
    private int convocatoriasLimit;

    @Column(name = "can_export_excel", nullable = false)
    private boolean canExportExcel;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
