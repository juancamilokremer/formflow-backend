package com.kodelabs.formflow.modules.forms.domain.port.out;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;

import java.util.List;
import java.util.UUID;

public interface ConvocatoriaFormRepositoryPort {

    ConvocatoriaForm save(ConvocatoriaForm form);

    List<ConvocatoriaForm> saveAll(List<ConvocatoriaForm> forms);

    List<ConvocatoriaForm> findAllByConvocatoriaId(UUID convocatoriaId);

    int countByConvocatoriaId(UUID convocatoriaId);

    void deleteById(UUID id);
}
