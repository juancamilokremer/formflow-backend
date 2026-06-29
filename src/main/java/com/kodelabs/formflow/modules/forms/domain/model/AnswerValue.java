package com.kodelabs.formflow.modules.forms.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerValue {

    private UUID id;
    private UUID responseId;
    private UUID questionId;

    /**
     * Polymorphic JSON value: String, Number, List<String>, or Map<String,Object>
     * depending on the question type.
     */
    private Object value;
}
