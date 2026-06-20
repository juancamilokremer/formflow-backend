package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public record ImportCandidatesCommand(
        UUID convocatoriaId,
        UUID tenantId,
        UUID userId,
        byte[] csvContent
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImportCandidatesCommand that)) return false;
        return Objects.equals(convocatoriaId, that.convocatoriaId)
                && Objects.equals(tenantId, that.tenantId)
                && Objects.equals(userId, that.userId)
                && Arrays.equals(csvContent, that.csvContent);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(convocatoriaId, tenantId, userId);
        result = 31 * result + Arrays.hashCode(csvContent);
        return result;
    }

    @Override
    public String toString() {
        return "ImportCandidatesCommand[convocatoriaId=" + convocatoriaId
                + ", tenantId=" + tenantId
                + ", userId=" + userId
                + ", csvContent.length=" + (csvContent != null ? csvContent.length : 0) + "]";
    }
}
