package com.kodelabs.formflow.modules.forms.domain.model;

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
public class Category {

    private UUID id;
    private UUID tenantId;
    private String name;
    private String color;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
