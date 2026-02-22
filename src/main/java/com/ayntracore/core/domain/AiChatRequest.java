// Autor: Christian Langner
package com.ayntracore.core.domain;

import java.util.List;
import java.util.Map;

/**
 * Domain-Objekt für AI-Chat-Anfragen.
 * Bündelt System-Prompts, User-Messages und Einstellungen.
 */
public record AiChatRequest(
        String systemPrompt,
        String userMessage,
        List<Map<String, Object>> messages, // Hinzugefügt
        double temperature,
        AiProvider preferredProvider,
        String model,
        Map<String, Object> additionalParameters
) {
    public static AiChatRequest of(String systemPrompt, String userMessage) {
        return new AiChatRequest(systemPrompt, userMessage, null, 0.7, null, null, Map.of());
    }

    public static AiChatRequest of(String systemPrompt, String userMessage, double temperature) {
        return new AiChatRequest(systemPrompt, userMessage, null, temperature, null, null, Map.of());
    }

    public List<Map<String, Object>> getMessages() {
        return messages;
    }
}
