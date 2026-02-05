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
 * KI-Adapter für die Ticket-Analyse via OpenRouter/OpenAI (Outbound-Adapter).
 * 
 * <p>Diese Klasse ist ein <strong>Outbound-Adapter</strong> in der Hexagonalen Architektur
 * und implementiert das {@link AiServicePort}-Interface. Sie verbindet die Domain-Schicht
 * mit externen KI-Diensten und unterstützt <strong>RAG (Retrieval-Augmented Generation)</strong>.</p>
 * 
 * <h2>Warum ist dieser Adapter wichtig?</h2>
 * <ul>
 *   <li><strong>KI-Integration:</strong> Ermöglicht automatische Ticket-Analyse und
 *       Antwortgenerierung durch Large Language Models (LLMs).</li>
 *   <li><strong>RAG-Unterstützung:</strong> Kombiniert KI-Wissen mit firmenspezifischem
 *       Kontext aus der Wissensdatenbank für präzisere Antworten.</li>
 *   <li><strong>Entkopplung:</strong> Die Domain kennt keine REST-APIs oder HTTP-Details.
 *       Dieser Adapter übersetzt Domain-Anfragen in API-Calls.</li>
 *   <li><strong>Austauschbarkeit:</strong> KI-Anbieter kann gewechselt werden (OpenAI,
 *       Anthropic, lokale Modelle) ohne Domain-Änderungen.</li>
 * </ul>
 * 
 * <h2>Architektur-Kontext:</h2>
 * <pre>
 * Domain Layer → AiServicePort (Interface) → OpenAiAdapter (hier) → OpenRouter API
 * </pre>
 * 
 * <h2>RAG-Workflow:</h2>
 * <ol>
 *   <li>Ticket-Nachricht wird empfangen</li>
 *   <li>Kontext aus Wissensdatenbank wird hinzugefügt (vom Service bereitgestellt)</li>
 *   <li>System-Prompt wird mit Kontext angereichert</li>
 *   <li>API-Request an OpenRouter wird gesendet</li>
 *   <li>KI-Antwort wird extrahiert und zurückgegeben</li>
 * </ol>
 * 
 * <h2>Konfiguration (application.properties):</h2>
 * <pre>
 * ai.api.url=https://openrouter.ai/api/v1/chat/completions
 * ai.api.key=sk-or-v1-...
 * ai.model=openai/gpt-4o-mini
 * </pre>
 * 
 * <p><strong>Hinweis:</strong> OpenRouter ist ein Gateway, das Zugriff auf verschiedene
 * KI-Modelle bietet (OpenAI, Anthropic, Meta, etc.) über eine einheitliche API.</p>
 * 
 * @author SyntraCore Development Team
 * @version 3.0
 * @since 2.0
 * 
 * @see com.syntracore.core.ports.AiServicePort
 * @see com.syntracore.core.domain.SupportTicket
 * @see com.syntracore.core.services.TicketService
 */
@Component
public class OpenAiAdapter implements AiServicePort {

    /**
     * Spring RestClient für HTTP-Kommunikation mit der KI-API.
     * 
     * <p>Wird im Konstruktor mit der Basis-URL konfiguriert. RestClient ist die
     * moderne Alternative zu RestTemplate (seit Spring 6).</p>
     */
    private final RestClient restClient;

    /**
     * Name des zu verwendenden KI-Modells.
     * 
     * <p>Wird aus {@code application.properties} injiziert. Beispiele:</p>
     * <ul>
     *   <li>{@code openai/gpt-4o-mini} - Schnell und günstig</li>
     *   <li>{@code openai/gpt-4} - Höchste Qualität</li>
     *   <li>{@code anthropic/claude-3-opus} - Alternative zu GPT-4</li>
     * </ul>
     */
    @Value("${ai.model}")
    private String model;

    /**
     * API-Schlüssel für die Authentifizierung bei OpenRouter.
     * 
     * <p>Wird aus {@code application.properties} injiziert. Format: {@code sk-or-v1-...}</p>
     * 
     * <p><strong>⚠️ Sicherheitshinweis:</strong> API-Keys sollten niemals im Code
     * hart-codiert oder in Git committed werden. Verwende Umgebungsvariablen oder
     * Secret-Management-Tools in Produktion.</p>
     */
    @Value("${ai.api.key}")
    private String apiKey;

    /**
     * Konstruktor mit Dependency Injection für RestClient.
     * 
     * <p>Spring injiziert automatisch einen {@link RestClient.Builder} und die
     * API-URL aus den Properties. Der Builder wird mit der Basis-URL konfiguriert.</p>
     * 
     * @param builder Spring's RestClient.Builder für HTTP-Kommunikation
     * @param apiUrl Die Basis-URL der KI-API (aus application.properties)
     */
    public OpenAiAdapter(RestClient.Builder builder, @Value("${ai.api.url}") String apiUrl) {
        // Wir konfigurieren den Client mit der Basis-URL von OpenRouter
        // Alle Requests werden relativ zu dieser URL gemacht
        this.restClient = builder.baseUrl(apiUrl).build();
    }

    /**
     * Generiert eine KI-basierte Analyse für ein Support-Ticket mit RAG-Unterstützung.
     * 
     * <p>Diese Methode implementiert den vollständigen RAG-Workflow:</p>
     * <ol>
     *   <li>Kontext aus Wissensdatenbank wird validiert</li>
     *   <li>System-Prompt wird mit Kontext angereichert</li>
     *   <li>API-Request wird zusammengebaut (JSON-Format)</li>
     *   <li>HTTP POST-Request an OpenRouter wird gesendet</li>
     *   <li>Response wird geparst und KI-Antwort extrahiert</li>
     * </ol>
     * 
     * <h3>Request-Format (OpenAI Chat Completions API):</h3>
     * <pre>
     * {
     *   "model": "openai/gpt-4o-mini",
     *   "messages": [
     *     {
     *       "role": "system",
     *       "content": "Du bist ein Support-Experte. Nutze AUSSCHLIESSLICH das folgende Wissen..."
     *     },
     *     {
     *       "role": "user",
     *       "content": "Problem des Kunden: Login funktioniert nicht"
     *     }
     *   ]
     * }
     * </pre>
     * 
     * <h3>Response-Format:</h3>
     * <pre>
     * {
     *   "choices": [
     *     {
     *       "message": {
     *         "content": "Basierend auf dem Handbuch empfehle ich..."
     *       }
     *     }
     *   ]
     * }
     * </pre>
     * 
     * <p><strong>Verwendungsbeispiel:</strong></p>
     * <pre>
     * SupportTicket ticket = new SupportTicket("Max", "Passwort vergessen");
     * String context = "HANDBUCH: Passwort-Reset über Portal möglich";
     * String analysis = openAiAdapter.generateAnalysis(ticket, context);
     * </pre>
     * 
     * @param ticket Das zu analysierende Support-Ticket (darf nicht null sein)
     * @param context Der Kontext aus der Wissensdatenbank (kann leer sein)
     * 
     * @return Die von der KI generierte Analyse als String. Niemals null.
     *         Bei Fehlern wird eine Fehlermeldung zurückgegeben.
     * 
     * @throws NullPointerException wenn ticket null ist
     */
    @Override
    public String generateAnalysis(SupportTicket ticket, String context) {
        try {
            // Schritt 1: RAG-Logik - Kontext validieren und vorbereiten
            // Falls kein Kontext gefunden wurde, geben wir einen Standard-Satz mit.
            String promptContext = (context != null && !context.isEmpty()) 
                ? context 
                : "Keine spezifischen Handbuch-Infos gefunden.";

            // Schritt 2: Request-Body zusammenbauen (OpenAI Chat Completions Format)
            // Wir verwenden das "Messages"-Format mit System- und User-Rolle
            var requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            // System-Prompt: Definiert die Rolle und das Verhalten der KI
                            Map.of("role", "system", "content",
                                    "Du bist ein Support-Experte. Nutze AUSSCHLIESSLICH das folgende Wissen aus dem Handbuch für deine Analyse: " + promptContext),
                            // User-Prompt: Die eigentliche Anfrage des Kunden
                            Map.of("role", "user", "content", "Problem des Kunden: " + ticket.getMessage())
                    )
            );

            // Schritt 3: HTTP POST-Request an OpenRouter senden
            var response = restClient.post()
                    .header("Authorization", "Bearer " + apiKey)  // Authentifizierung
                    .contentType(MediaType.APPLICATION_JSON)       // JSON-Content
                    .body(requestBody)                             // Request-Body
                    .retrieve()                                    // Request ausführen
                    .body(Map.class);                              // Response als Map parsen

            // Schritt 4: KI-Antwort aus der JSON-Response extrahieren
            // Die Response hat die Struktur: { "choices": [ { "message": { "content": "..." } } ] }
            if (response != null && response.get("choices") != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, String> message = (Map<String, String>) firstChoice.get("message");
                return message.get("content");
            }

            // Fallback, falls die Response-Struktur unerwartet ist
            return "KI-Dienst hat keine Antwort geliefert.";

        } catch (Exception e) {
            // Fehlerbehandlung: Netzwerkfehler, ungültiger API-Key, Rate-Limits, etc.
            // In Produktion sollte hier ein Logger verwendet werden
            return "Fehler bei der KI-Analyse: " + e.getMessage();
        }
    }
}