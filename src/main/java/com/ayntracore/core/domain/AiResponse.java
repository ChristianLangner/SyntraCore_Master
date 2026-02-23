// Autor: Christian Langner
package com.ayntracore.core.domain;

/**
 * Bündelt die Antwort der AI mit den zugehörigen Metadaten.
 */
public record AiResponse(
        String content,
        AiResponseMetadata metadata
) {
    public static AiResponse error(String errorMessage) {
        return new AiResponse(errorMessage, new AiResponseMetadata(null, 0, 0, 0, AiProvider.OPENAI, "error"));
    }
}
