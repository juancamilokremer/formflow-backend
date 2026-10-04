package com.kodelabs.formflow.shared.web.dto.response;

import java.time.Instant;

/** Deliberately not wrapped in ApiResponse — this is a plain status-check payload for
 *  external uptime monitors (Upptime) and humans, not a business API response. */
public record PublicHealthResponse(String status, String version, Instant timestamp) {

    public static PublicHealthResponse up(String version) {
        return new PublicHealthResponse("ok", version, Instant.now());
    }
}
