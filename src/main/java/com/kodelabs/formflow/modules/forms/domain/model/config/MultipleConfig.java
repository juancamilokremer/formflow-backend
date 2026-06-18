package com.kodelabs.formflow.modules.forms.domain.model.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultipleConfig extends QuestionConfig implements Validatable {

    @Builder.Default
    private List<AnswerOption> options = new ArrayList<>();
    private Integer maxSelections;
    @Builder.Default
    private boolean randomize = false;

    @Override
    public void validate() {
        require(options, "options");
    }
}
