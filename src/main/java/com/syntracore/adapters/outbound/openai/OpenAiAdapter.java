// UPDATE #3
// Notiz: Anpassung an das AiServicePort-Interface. Der System-Prompt nutzt nun den 'context' aus der Wissensdatenbank (RAG).
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
 * Outbound Adapter für die KI-Analyse via OpenRouter.
 * Jetzt mit RAG-Unterstützung: Verarbeitet zusätzliches Wissen aus dem Handbuch.
 */
@Component
public class OpenAiAdapter implements AiServicePort {

    private final RestClient restClient;

    @Value("${ai.model}")
    private String model;

    @Value("${ai.api.key}")
    private String apiKey;

    public OpenAiAdapter(RestClient.Builder builder, @Value("${ai.api.url}") String apiUrl) {
        // Wir konfigurieren den Client mit der Basis-URL von OpenRouter
        this.restClient = builder.baseUrl(apiUrl).build();
    }

    @Override
    public String generateAnalysis(SupportTicket ticket, String context) {
        try {
            // RAG-Logik: Wir füttern die KI mit dem Wissen, das wir im TicketService gefunden haben.
            // Falls kein Kontext gefunden wurde, geben wir einen Standard-Satz mit.
            String promptContext = (context != null && !context.isEmpty()) ? context : "Keine spezifischen Handbuch-Infos gefunden.";

            // Wir bauen den Brief (Request) für die KI zusammen
            var requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content",
                                    "Du bist ein Support-Experte. Nutze AUSSCHLIESSLICH das folgende Wissen aus dem Handbuch für deine Analyse: " + promptContext),
                            Map.of("role", "user", "content", "Problem des Kunden: " + ticket.getMessage())
                    )
            );

            // Jetzt schicken wir den Brief ab
            var response = restClient.post()
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class); // Wir erwarten eine Antwort im Map-Format (JSON)

            // Hier klauben wir uns die Antwort aus der JSON-Struktur raus
            if (response != null && response.get("choices") != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, String> message = (Map<String, String>) firstChoice.get("message");
                return message.get("content");
            }

            return "KI-Dienst hat keine Antwort geliefert.";

        } catch (Exception e) {
            // Falls das Internet weg ist oder der Key falsch, fangen wir das hier ab
            return "Fehler bei der KI-Analyse: " + e.getMessage();
        }
    }
}