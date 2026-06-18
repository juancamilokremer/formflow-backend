package com.kodelabs.formflow.modules.forms.domain.model.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileConfig extends QuestionConfig implements Validatable {

    @Builder.Default
    private int maxSizeMb = 5;
    private List<String> allowedTypes;

    @Override
    public void validate() {
        require(allowedTypes, "allowedTypes");
    }
}
