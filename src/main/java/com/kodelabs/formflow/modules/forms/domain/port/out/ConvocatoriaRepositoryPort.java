package com.kodelabs.formflow.modules.forms.domain.port.out;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConvocatoriaRepositoryPort {

    Convocatoria save(Convocatoria convocatoria);

    Optional<Convocatoria> findByIdAndTenantId(UUID id, UUID tenantId);

    /** Same as findByIdAndTenantId, but locks the row for the rest of the transaction.
     *  Use before a read-then-write on the convocatoria (position counters) that would
     *  otherwise race under concurrent requests. */
    Optional<Convocatoria> findByIdAndTenantIdForUpdate(UUID id, UUID tenantId);

    List<Convocatoria> findActiveByTenantId(UUID tenantId);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);

    void softDeleteById(UUID id);
}
