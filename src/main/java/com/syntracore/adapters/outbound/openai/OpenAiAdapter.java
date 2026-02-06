// UPDATE #5: Korrektur des API-Pfads für OpenRouter
// Zweck: Sicherstellen, dass die Anfrage an den korrekten JSON-Endpunkt geht
// Ort: src/main/java/com/syntracore/adapters/outbound/openai/OpenAiAdapter.java

package com.syntracore.adapters.outbound.openai;

import com.syntracore.core.domain.SupportTicket;
import com.syntracore.core.ports.AiServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * KI-Adapter für die Kommunikation mit OpenRouter.
 * Implementiert den AiServicePort für Tickets und Live-Chat.
 */
@Component
public class OpenAiAdapter implements AiServicePort {

    private final RestClient restClient;

    @Value("${ai.model}")
    private String model;

    @Value("${ai.api.key}")
    private String apiKey;

    public OpenAiAdapter(RestClient.Builder builder, @Value("${ai.api.url}") String apiUrl) {
        // Wir nutzen die Basis-URL aus den Properties
        this.restClient = builder.baseUrl(apiUrl).build();
    }

    @Override
    public String generateAnalysis(SupportTicket ticket, String context, String systemPrompt, String speakingStyle) {
        return callOpenRouter("Problem des Kunden: " + ticket.getMessage(), context, systemPrompt, speakingStyle);
    }

    @Override
    public String generateChatResponse(String userPrompt, String context, String systemPrompt, String speakingStyle) {
        return callOpenRouter(userPrompt, context, systemPrompt, speakingStyle);
    }

    private boolean isApiKeyMissing() {
        return apiKey == null || apiKey.isBlank() || "__SET_ME__".equals(apiKey);
    }

    private String callOpenRouter(String input, String context, String systemPrompt, String speakingStyle) {
        try {
            if (isApiKeyMissing()) {
                return "KI ist nicht konfiguriert: Bitte setze OPENROUTER_API_KEY (oder ai.api.key) und starte neu.";
            }

            String promptContext = (context != null && !context.isEmpty()) ? context : "Allgemeiner Support.";

            String effectiveSystemPrompt = (systemPrompt == null || systemPrompt.isBlank())
                    ? "Du bist ein Support-Experte."
                    : systemPrompt;

            String effectiveStyle = (speakingStyle == null || speakingStyle.isBlank())
                    ? "Freundlich, klar, präzise."
                    : speakingStyle;

            var requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", effectiveSystemPrompt
                                            + "\n\nSprechstil: " + effectiveStyle
                                            + "\n\nWissen/Kontext:\n" + promptContext
                            ),
                            Map.of("role", "user", "content", input)
                    )
            );

            var response = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.get("choices") != null) {
                List choices = (List) response.get("choices");
                Map firstChoice = (Map) choices.get(0);
                Map message = (Map) firstChoice.get("message");
                return (String) message.get("content");
            }

            return "Fehler: KI lieferte eine leere Antwort.";

        } catch (Exception e) {
            return "Fehler bei der KI-Anfrage: " + e.getMessage();
        }
    }
}