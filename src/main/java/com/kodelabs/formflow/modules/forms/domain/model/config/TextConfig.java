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
public class TextConfig extends QuestionConfig {

    @Builder.Default
    private Integer maxLength = 2000;
    private String placeholder;
    @Builder.Default
    private Integer rows = 1;

    @Override
    public void validate() {
        // all fields are optional — no required fields for TEXT
    }
}
