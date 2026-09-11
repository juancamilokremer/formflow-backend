package com.kodelabs.formflow.shared.export;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelSheetNamesTest {

    @Test
    void returnsTheNameUnchangedWhenItIsAlreadyValidAndUnused() {
        assertThat(ExcelSheetNames.uniqueName("Evaluación técnica", Set.of())).isEqualTo("Evaluación técnica");
    }

    @Test
    void stripsCharactersForbiddenByExcel() {
        assertThat(ExcelSheetNames.uniqueName("Evaluación: Técnica/Blanda [2026]", Set.of()))
                .doesNotContain(":").doesNotContain("/").doesNotContain("[").doesNotContain("]");
    }

    @Test
    void truncatesNamesLongerThan31Characters() {
        String longName = "Un nombre de formulario extremadamente largo que supera el limite";
        String result = ExcelSheetNames.uniqueName(longName, Set.of());
        assertThat(result.length()).isLessThanOrEqualTo(31);
    }

    @Test
    void appendsANumberedSuffixWhenTheNameIsAlreadyUsed() {
        Set<String> used = new HashSet<>(Set.of("Formulario"));
        assertThat(ExcelSheetNames.uniqueName("Formulario", used)).isEqualTo("Formulario (2)");
    }

    @Test
    void keepsIncrementingTheSuffixUntilItFindsAFreeName() {
        Set<String> used = new HashSet<>(Set.of("Formulario", "Formulario (2)", "Formulario (3)"));
        assertThat(ExcelSheetNames.uniqueName("Formulario", used)).isEqualTo("Formulario (4)");
    }

    @Test
    void keepsTheSuffixedNameWithin31CharactersForALongBaseName() {
        String longName = "Un nombre de formulario extremadamente largo";
        Set<String> used = new HashSet<>(Set.of(ExcelSheetNames.uniqueName(longName, Set.of())));
        String result = ExcelSheetNames.uniqueName(longName, used);
        assertThat(result.length()).isLessThanOrEqualTo(31);
        assertThat(result).endsWith(" (2)");
    }

    @Test
    void fallsBackToADefaultNameWhenNothingIsLeftAfterSanitizing() {
        assertThat(ExcelSheetNames.uniqueName(":/", Set.of())).isEqualTo("Hoja");
    }
}
