package com.kodelabs.formflow.modules.forms.application.service.conditional.operator;

import java.util.Collection;

final class ConditionalValueHelper {

    private ConditionalValueHelper() {}

    static String stringify(Object obj) {
        return obj == null ? null : obj.toString();
    }

    static double toDouble(Object obj) {
        if (obj == null) return Double.NaN;
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    static boolean containsInCollection(Object answer, Object value) {
        if (answer instanceof Collection<?> col) {
            String target = stringify(value);
            return col.stream().map(Object::toString).anyMatch(v -> v.equals(target));
        }
        return answer != null && answer.toString().contains(value != null ? value.toString() : "");
    }
}
