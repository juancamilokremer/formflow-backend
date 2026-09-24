package com.kodelabs.formflow.shared.exception;

import com.kodelabs.formflow.shared.i18n.Messages;
import com.kodelabs.formflow.shared.web.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Routing/protocol exceptions used to fall through to handleGenericException (500 + a support
 * errorId meant for real server failures) instead of the client-error status they actually are.
 * See #150 — the trigger was retiring POST /forms in #148: an outdated client hitting the old
 * route got a confusing 500 instead of a 405.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock private Messages messages;
    @InjectMocks private GlobalExceptionHandler handler;

    /** Minimal stub: HttpMessageNotReadableException just needs a source to attribute the parse error to. */
    private static final HttpInputMessage EMPTY_BODY = new HttpInputMessage() {
        @Override public InputStream getBody() { return InputStream.nullInputStream(); }
        @Override public HttpHeaders getHeaders() { return new HttpHeaders(); }
    };

    @Test
    void wrongHttpMethodReturns405NotAServerError() throws Exception {
        when(messages.get(anyString(), any(), any())).thenReturn("Método POST no permitido. Métodos soportados: GET");
        var ex = new HttpRequestMethodNotSupportedException("POST", List.of("GET"));

        ResponseEntity<ApiResponse<Void>> response = handler.handleMethodNotAllowed(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody().getMessage()).contains("POST").contains("GET");
    }

    @Test
    void unmappedRouteReturns404() {
        when(messages.get("error.route_not_found")).thenReturn("Ruta no encontrada");
        var ex = new NoResourceFoundException(HttpMethod.GET, "api/v1/no-existe");

        ResponseEntity<ApiResponse<Void>> response = handler.handleRouteNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void unsupportedContentTypeReturns415() {
        when(messages.get("error.unsupported_media_type")).thenReturn("Tipo de contenido no soportado");
        var ex = new HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<ApiResponse<Void>> response = handler.handleUnsupportedMediaType(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    void malformedJsonBodyReturns400WithoutLeakingParserInternals() {
        when(messages.get("error.malformed_request")).thenReturn("El cuerpo de la solicitud no tiene un formato válido");
        var ex = new HttpMessageNotReadableException(
                "JSON parse error: Unexpected character ('r' (code 114))", EMPTY_BODY);

        ResponseEntity<ApiResponse<Void>> response = handler.handleMalformedRequest(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        // The Jackson parser detail (field names, token types) never reaches the client.
        assertThat(response.getBody().getMessage()).doesNotContain("JSON parse error");
    }

    @Test
    void noneOfTheseFourGenerateASupportErrorId() {
        when(messages.get(anyString(), any(), any())).thenReturn("x");
        when(messages.get(anyString())).thenReturn("x");

        String methodMsg = handler.handleMethodNotAllowed(
                new HttpRequestMethodNotSupportedException("POST", List.of("GET"))).getBody().getMessage();
        String routeMsg = handler.handleRouteNotFound(
                new NoResourceFoundException(HttpMethod.GET, "x")).getBody().getMessage();
        String mediaMsg = handler.handleUnsupportedMediaType(
                new HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON)))
                .getBody().getMessage();
        String bodyMsg = handler.handleMalformedRequest(
                new HttpMessageNotReadableException("bad", EMPTY_BODY))
                .getBody().getMessage();

        // A support errorId is an 8-char hex-ish token wrapped in parentheses — none of these
        // client errors should carry one, that format is reserved for handleGenericException.
        assertThat(methodMsg).doesNotContain("código de soporte");
        assertThat(routeMsg).doesNotContain("código de soporte");
        assertThat(mediaMsg).doesNotContain("código de soporte");
        assertThat(bodyMsg).doesNotContain("código de soporte");
    }
}
