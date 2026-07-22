package com.kodelabs.formflow.modules.forms.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormTest {

    @Test
    void isLocked_returnsFalse_whenDraft() {
        Form form = Form.builder().status(FormStatus.DRAFT).type(FormType.CANDIDATES).build();
        assertThat(form.isLocked()).isFalse();
    }

    @Test
    void isLocked_returnsTrue_whenActiveAndCandidates() {
        Form form = Form.builder().status(FormStatus.ACTIVE).type(FormType.CANDIDATES).build();
        assertThat(form.isLocked()).isTrue();
    }

    @Test
    void isLocked_returnsTrue_whenArchivedAndDiagnostic() {
        Form form = Form.builder().status(FormStatus.ARCHIVED).type(FormType.DIAGNOSTIC).build();
        assertThat(form.isLocked()).isTrue();
    }

    @Test
    void isLocked_returnsFalse_whenRegistrationRegardlessOfStatus() {
        Form active = Form.builder().status(FormStatus.ACTIVE).type(FormType.REGISTRATION).build();
        Form archived = Form.builder().status(FormStatus.ARCHIVED).type(FormType.REGISTRATION).build();

        assertThat(active.isLocked()).isFalse();
        assertThat(archived.isLocked()).isFalse();
    }
}
