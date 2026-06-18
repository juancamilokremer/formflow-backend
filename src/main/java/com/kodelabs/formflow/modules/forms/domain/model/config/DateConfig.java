package com.kodelabs.formflow.modules.forms.domain.model.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DateConfig extends QuestionConfig {

    @Builder.Default
    private boolean includeTime = false;
    private String minDate;
    private String maxDate;

    @Override
    public void validate() {
        // all fields optional
    }
}
