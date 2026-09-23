package com.kodelabs.formflow.shared.i18n;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards against a Spanish key being added without its English counterpart (or vice versa) —
 * without this, a missing key silently falls back to the raw key string for that locale.
 */
class MessagesLocaleCompletenessTest {

    @Test
    void esAndEnHaveTheExactSameKeySet() throws IOException {
        Set<String> esKeys = loadKeys("messages_es.properties");
        Set<String> enKeys = loadKeys("messages_en.properties");

        Set<String> missingInEn = new TreeSet<>(esKeys);
        missingInEn.removeAll(enKeys);
        Set<String> missingInEs = new TreeSet<>(enKeys);
        missingInEs.removeAll(esKeys);

        assertThat(missingInEn).as("keys present in messages_es.properties but missing in messages_en.properties").isEmpty();
        assertThat(missingInEs).as("keys present in messages_en.properties but missing in messages_es.properties").isEmpty();
    }

    private Set<String> loadKeys(String resourceName) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            assertThat(in).as(resourceName + " must exist on the classpath").isNotNull();
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return new HashSet<>(properties.stringPropertyNames());
    }
}
