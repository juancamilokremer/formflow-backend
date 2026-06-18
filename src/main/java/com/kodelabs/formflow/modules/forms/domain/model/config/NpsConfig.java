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
public class NpsConfig extends QuestionConfig {

    @Builder.Default
    private String minLabel = "Nada probable";
    @Builder.Default
    private String maxLabel = "Extremadamente probable";

    @Override
    public void validate() {
        // labels are optional, NPS is always 0-10
    }
}
