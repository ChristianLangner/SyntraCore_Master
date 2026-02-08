// Autor: Christian Langner
package com.ayntracore.core.domain;

/**
 * Metadaten einer AI-Antwort.
 */
public record AiResponseMetadata(
        String modelName,
        long promptTokens,
        long completionTokens,
        long totalTokens,
        AiProvider providerType,
        String finishReason
) {
}
