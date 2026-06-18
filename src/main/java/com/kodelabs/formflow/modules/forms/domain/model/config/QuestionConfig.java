package com.kodelabs.formflow.modules.forms.domain.model.config;

import com.kodelabs.formflow.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public abstract class QuestionConfig {

    public abstract void validate();

    protected void require(Object value, String field) {
        if (value == null) {
            throw new BusinessException("error.question.config_missing_field",
                    HttpStatus.BAD_REQUEST, field);
        }
    }
}
