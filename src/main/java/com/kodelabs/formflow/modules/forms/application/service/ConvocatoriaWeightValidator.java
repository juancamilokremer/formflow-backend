package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConvocatoriaWeightValidator {

    public void validate(List<CategoryWeight> weights) {
        if (weights == null || weights.isEmpty()) return;
        int total = weights.stream().mapToInt(CategoryWeight::weight).sum();
        if (total != 100) {
            throw new BusinessException("error.convocatoria.weights_must_sum_100", HttpStatus.BAD_REQUEST, total);
        }
    }
}
