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
public class MatrixConfig extends QuestionConfig {

    @Builder.Default
    private List<MatrixRow> rows = new ArrayList<>();
    @Builder.Default
    private List<MatrixColumn> columns = new ArrayList<>();

    @Override
    public void validate() {
        require(rows, "rows");
        require(columns, "columns");
    }
}
