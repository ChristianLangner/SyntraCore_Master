// Autor: Christian Langner
// UPDATE #5: Korrektur des API-Pfads für OpenRouter
// Zweck: Sicherstellen, dass die Anfrage an den korrekten JSON-Endpunkt geht
// Ort: src/main/java/com/ayntracore/adapters/outbound/openai/OpenAiAdapter.java

package com.ayntracore.adapters.outbound.openai;

import com.ayntracore.core.domain.Persona;
import com.ayntracore.core.domain.SupportTicket;
import com.ayntracore.core.ports.AiServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Outbound Adapter für AI-Service (OpenRouter/OpenAI/DeepSeek).
 *
 * <p><strong>Architektur-Schicht:</strong> Infrastructure Layer (Outbound Adapter)</p>
 * <p><strong>Hexagonale Architektur:</strong> Implementiert AiServicePort</p>
 *
 * <h2>API-Integration:</h2>
 * <ul>
 *   <li><strong>Provider:</strong> OpenRouter (proxy for multiple LLMs)</li>
 *   <li><strong>Model:</strong> deepseek/deepseek-chat (configurable)</li>
 *   <li><strong>Endpoint:</strong> /chat/completions (OpenAI-compatible)</li>
 *   <li><strong>Authentication:</strong> Bearer Token via OPENROUTER_API_KEY</li>
 * </ul>
 *
 * <h2>Persona-Integration:</h2>
 * <ul>
 *   <li><strong>System Prompt:</strong> Dynamisch aus Persona generiert</li>
 *   <li><strong>Traits:</strong> Als strukturierter Text im Prompt</li>
 *   <li><strong>Example Dialog:</strong> Als Assistant-Message für Few-Shot Learning</li>
 *   <li><strong>Template:</strong> Unterstützt Platzhalter-Ersetzung</li>
 * </ul>
 *
 * @author Christian Langner
 * @version 2.0
 * @since 2026
 *
 * @see AiServicePort
 * @see Persona
 */
@Component
@Profile("home")
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

    /**
     * Ruft OpenRouter/OpenAI API mit Persona-basiertem Prompt auf.
     *
     * <h2>Error Handling:</h2>
     * <ul>
     *   <li><strong>Missing API Key:</strong> User-friendly Fehlermeldung</li>
     *   <li><strong>Network Timeout:</strong> Exception wird gefangen und geloggt</li>
     *   <li><strong>Invalid Response:</strong> Fallback-Meldung</li>
     * </ul>
     *
     * @param input User-Input oder Ticket-Message
     * @param context RAG-Kontext aus Vector-Suche
     * @param persona Aktive Persona mit Prompt-Template
     * @return LLM-Response oder Fehlermeldung
     */
    private String callOpenRouter(String input, String context, Persona persona) {
        try {
            // 1. API-Key-Validierung
            if (isApiKeyMissing()) {
                return "KI ist nicht konfiguriert: Bitte setze OPENROUTER_API_KEY (oder ai.api.key) und starte neu.";
            }

            // 2. Master-System-Prompt aus Persona generieren
            String masterSystemPrompt = buildMasterSystemPrompt(context, persona);

            // 3. Messages-Array aufbauen
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", masterSystemPrompt));

            // 4. Few-Shot Example Dialog hinzufügen (falls vorhanden)
            if (persona != null && persona.getExampleDialog() != null && !persona.getExampleDialog().isBlank()) {
                messages.add(Map.of("role", "assistant", "content", persona.getExampleDialog()));
            }

            // 5. User-Input
            messages.add(Map.of("role", "user", "content", input));

            // 6. Request-Body
            var requestBody = Map.of(
                    "model", model,
                    "messages", messages
            );

            // 7. API-Aufruf mit Timeout-Handling
            var response = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            // 8. Response-Verarbeitung
            if (response != null && response.get("choices") != null) {
                List choices = (List) response.get("choices");
                if (!choices.isEmpty()) {
                    Map firstChoice = (Map) choices.get(0);
                    Map message = (Map) firstChoice.get("message");
                    return (String) message.get("content");
                }
            }

            return "Fehler: KI lieferte eine leere Antwort.";

        } catch (Exception e) {
            // Error-Logging für Debugging
            String errorMsg = "Fehler bei der KI-Anfrage: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            return errorMsg;
        }
    }
}