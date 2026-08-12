package com.kodelabs.formflow.modules.reports.application.service.export;

import com.kodelabs.formflow.modules.reports.domain.model.ExportFormat;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ResponseExporterRegistry {

    private final Map<ExportFormat, ResponseExporter> exporters;

    public ResponseExporterRegistry(List<ResponseExporter> exporterList) {
        Map<ExportFormat, ResponseExporter> map = new LinkedHashMap<>();
        exporterList.forEach(exporter -> {
            if (map.containsKey(exporter.format())) {
                throw new IllegalStateException("Duplicate ResponseExporter format: " + exporter.format());
            }
            map.put(exporter.format(), exporter);
        });
        this.exporters = Map.copyOf(map);
    }

    public Optional<ResponseExporter> find(ExportFormat format) {
        return Optional.ofNullable(exporters.get(format));
    }
}
