package com.kodelabs.formflow.modules.forms.infrastructure.web.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record ReorderSectionsRequest(@NotEmpty List<UUID> orderedSectionIds) {}
