package com.kodelabs.formflow.modules.forms.domain.port.out;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;

import java.util.Optional;
import java.util.UUID;

public interface ConvocatoriaFormRepositoryPort {

    ConvocatoriaForm save(ConvocatoriaForm form);

    Optional<ConvocatoriaForm> findByConvocatoriaId(UUID convocatoriaId);
}
