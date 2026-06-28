package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

class ConvocatoriaWeightValidatorTest {

    private final ConvocatoriaWeightValidator validator = new ConvocatoriaWeightValidator();

    @Test
    void nullWeightsAreValid() {
        assertThatNoException().isThrownBy(() -> validator.validate(null));
    }

    @Test
    void emptyWeightsAreValid() {
        assertThatNoException().isThrownBy(() -> validator.validate(List.of()));
    }

    @Test
    void exactlyOneHundredIsValid() {
        var weights = List.of(
                new CategoryWeight(UUID.randomUUID(), 40),
                new CategoryWeight(UUID.randomUUID(), 35),
                new CategoryWeight(UUID.randomUUID(), 25));
        assertThatNoException().isThrownBy(() -> validator.validate(weights));
    }

    @Test
    void throwsBadRequestWhenWeightsDoNotSumTo100() {
        var weights = List.of(
                new CategoryWeight(UUID.randomUUID(), 40),
                new CategoryWeight(UUID.randomUUID(), 30));
        assertThatThrownBy(() -> validator.validate(weights))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void throwsBadRequestWhenWeightsExceed100() {
        var weights = List.of(
                new CategoryWeight(UUID.randomUUID(), 60),
                new CategoryWeight(UUID.randomUUID(), 60));
        assertThatThrownBy(() -> validator.validate(weights))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
