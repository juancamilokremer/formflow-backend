package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.application.service.handler.QuestionTypeHandler;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class QuestionTypeRegistry {

    private final Map<QuestionType, QuestionTypeHandler<?>> handlers;

    public QuestionTypeRegistry(List<QuestionTypeHandler<?>> handlerList) {
        Map<QuestionType, QuestionTypeHandler<?>> map = new LinkedHashMap<>();
        handlerList.stream()
                .sorted((a, b) -> a.type().code().compareTo(b.type().code()))
                .forEach(handler -> {
                    if (map.containsKey(handler.type())) {
                        throw new IllegalStateException("Duplicate QuestionType: " + handler.type().code());
                    }
                    map.put(handler.type(), handler);
                });
        this.handlers = Collections.unmodifiableMap(map);
    }

    public QuestionTypeHandler<?> get(QuestionType type) {
        QuestionTypeHandler<?> handler = handlers.get(type);
        if (handler == null) {
            throw new BusinessException("error.question.type_unknown", HttpStatus.BAD_REQUEST, type.code());
        }
        return handler;
    }

    public Collection<QuestionTypeHandler<?>> all() {
        return handlers.values();
    }
}
