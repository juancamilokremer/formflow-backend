package com.kodelabs.formflow.shared.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Thin wrapper around MessageSource using the request locale.
 * If a key is missing, the key itself is returned (never throws).
 */
@Component
@RequiredArgsConstructor
public class Messages {

    private final MessageSource messageSource;

    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }

    /**
     * For callers that run before Spring MVC resolves the request locale into
     * LocaleContextHolder (e.g. a servlet Filter, which runs ahead of DispatcherServlet) —
     * get(key, args) would otherwise silently fall back to the JVM's default locale, which
     * differs by machine/CI runner instead of respecting the client's Accept-Language.
     */
    public String getForLocale(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, key, locale);
    }
}
