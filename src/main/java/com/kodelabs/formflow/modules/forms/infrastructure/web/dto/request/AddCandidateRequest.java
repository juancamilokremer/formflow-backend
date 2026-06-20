package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCandidateRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Email @Size(max = 150) String email
) {}
