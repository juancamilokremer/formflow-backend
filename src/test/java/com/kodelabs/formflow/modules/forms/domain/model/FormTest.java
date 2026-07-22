package com.kodelabs.formflow.modules.forms.domain.model;

import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void assertEditable_doesNotThrow_whenNotLocked() {
        Form form = Form.builder().status(FormStatus.DRAFT).type(FormType.CANDIDATES).build();
        assertThatCode(form::assertEditable).doesNotThrowAnyException();
    }

    @Test
    void assertEditable_throwsBadRequest_whenLocked() {
        Form form = Form.builder().status(FormStatus.ACTIVE).type(FormType.CANDIDATES).build();
        assertThatThrownBy(form::assertEditable)
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.question.form_locked")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void assertLockedForVersioning_doesNotThrow_whenLocked() {
        Form form = Form.builder().status(FormStatus.ARCHIVED).type(FormType.CANDIDATES).build();
        assertThatCode(form::assertLockedForVersioning).doesNotThrowAnyException();
    }

    @Test
    void assertLockedForVersioning_throwsBadRequest_whenNotLocked() {
        Form form = Form.builder().status(FormStatus.DRAFT).type(FormType.CANDIDATES).build();
        assertThatThrownBy(form::assertLockedForVersioning)
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.form.version_source_not_locked")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
