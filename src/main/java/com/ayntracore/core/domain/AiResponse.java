// Autor: Christian Langner
package com.ayntracore.core.domain;

/**
 * Bündelt die Antwort der AI mit den zugehörigen Metadaten.
 */
public record AiResponse(
        String content,
        AiResponseMetadata metadata
) {
}
