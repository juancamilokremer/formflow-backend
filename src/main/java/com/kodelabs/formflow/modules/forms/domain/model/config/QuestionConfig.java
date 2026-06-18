package com.kodelabs.formflow.modules.forms.domain.model.config;

import com.kodelabs.formflow.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public abstract class QuestionConfig {

    /** Maximum score a respondent can obtain for a question with this config. */
    public int maxScore() {
        return 0;
    }

    protected void require(Object value, String field) {
        if (value == null) {
            throw new BusinessException("error.question.config_missing_field",
                    HttpStatus.BAD_REQUEST, field);
        }
    }
}
