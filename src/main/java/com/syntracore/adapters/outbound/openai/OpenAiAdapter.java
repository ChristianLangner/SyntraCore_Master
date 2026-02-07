// UPDATE #5: Korrektur des API-Pfads für OpenRouter
// Zweck: Sicherstellen, dass die Anfrage an den korrekten JSON-Endpunkt geht
// Ort: src/main/java/com/syntracore/adapters/outbound/openai/OpenAiAdapter.java

package com.syntracore.adapters.outbound.openai;

import com.syntracore.core.domain.Persona;
import com.syntracore.core.domain.SupportTicket;
import com.syntracore.core.ports.AiServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * // UPDATE #57
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
        this.restClient = builder.baseUrl(apiUrl).build();
    }

    @Override
    public String generateAnalysis(SupportTicket ticket, String context, Persona persona) {
        return callOpenRouter("Problem des Kunden: " + ticket.getMessage(), context, persona);
    }

    @Override
    public String generateChatResponse(String userPrompt, String context, Persona persona) {
        return callOpenRouter(userPrompt, context, persona);
    }

    private boolean isApiKeyMissing() {
        return apiKey == null || apiKey.isBlank() || "__SET_ME__".equals(apiKey);
    }

    private String renderTraits(Persona persona) {
        if (persona == null || persona.getTraits() == null || persona.getTraits().isEmpty()) {
            return "- (keine)";
        }
        StringBuilder sb = new StringBuilder();
        persona.getTraits().forEach((k, v) -> sb.append("- ").append(k).append(": ").append(v).append("\n"));
        return sb.toString().trim();
    }

    private String buildMasterSystemPrompt(String context, Persona persona) {
        String promptContext = (context != null && !context.isEmpty()) ? context : "Allgemeiner Support.";

        String effectiveSystemPrompt = (persona == null || persona.getSystemPrompt() == null || persona.getSystemPrompt().isBlank())
                ? "Du bist ein Support-Experte."
                : persona.getSystemPrompt();

        String effectiveStyle = (persona == null || persona.getSpeakingStyle() == null || persona.getSpeakingStyle().isBlank())
                ? "Freundlich, klar, präzise."
                : persona.getSpeakingStyle();

        String effectiveName = (persona == null || persona.getName() == null || persona.getName().isBlank())
                ? "Support Assistant"
                : persona.getName();

        String traitsBlock = renderTraits(persona);

        String template = (persona == null) ? null : persona.getPromptTemplate();
        if (template == null || template.isBlank()) {
            template = Persona.defaultTemplate();
        }

        String prompt = template
                .replace("{{systemPrompt}}", effectiveSystemPrompt)
                .replace("{{speakingStyle}}", effectiveStyle)
                .replace("{{name}}", effectiveName)
                .replace("{{traits}}", traitsBlock)
                .replace("{{context}}", promptContext);

        return prompt;
    }

    private String callOpenRouter(String input, String context, Persona persona) {
        try {
            if (isApiKeyMissing()) {
                return "KI ist nicht konfiguriert: Bitte setze OPENROUTER_API_KEY (oder ai.api.key) und starte neu.";
            }

            String masterSystemPrompt = buildMasterSystemPrompt(context, persona);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", masterSystemPrompt));

            if (persona != null && persona.getExampleDialog() != null && !persona.getExampleDialog().isBlank()) {
                messages.add(Map.of("role", "assistant", "content", persona.getExampleDialog()));
            }

            messages.add(Map.of("role", "user", "content", input));

            var requestBody = Map.of(
                    "model", model,
                    "messages", messages
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