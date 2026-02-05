package com.syntracore.core.ports;

import com.syntracore.core.domain.SupportTicket;

/**
 * Outbound Port – Driven Port für KI-basierte Ticket-Analyse.
 * <p>
 * Definiert die Anforderungen der Domain-Schicht an externe KI-Dienste.
 * Ermöglicht die Integration verschiedener KI-Anbieter mittels des
 * Retrieval-Augmented Generation (RAG) Workflows.
 * </p>
 * @see Outbound-Port gemäß hexagonaler Architektur
 * @author Christian Langner
 * @version 2.0
 * @since 2026
 */
public interface AiServicePort {
    
    /**
     * Generiert eine KI-basierte Analyse für ein Support-Ticket unter Berücksichtigung
     * von zusätzlichem Wissen aus der Wissensdatenbank (RAG-Ansatz).
     * 
     * <p>Die Methode kombiniert die Ticket-Nachricht mit relevantem Kontext aus dem
     * Firmenhandbuch, um eine präzise und hilfreiche Antwort zu generieren.</p>
     * 
     * <p><strong>Ablauf:</strong></p>
     * <ol>
     *   <li>Ticket-Nachricht wird analysiert</li>
     *   <li>Kontext aus Wissensdatenbank wird einbezogen</li>
     *   <li>KI generiert Antwort basierend auf beidem</li>
     * </ol>
     * 
     * <p><strong>Verwendungsbeispiel:</strong></p>
     * <pre>
     * SupportTicket ticket = new SupportTicket("Max", "Passwort vergessen");
     * String context = "HANDBUCH: Passwort-Reset über Portal möglich";
     * String analysis = aiService.generateAnalysis(ticket, context);
     * </pre>
     * 
     * @param ticket Das zu analysierende Support-Ticket (darf nicht null sein)
     * @param context Der gefundene Text aus der Wissensdatenbank, der als Kontext
     *                für die KI-Analyse dient. Kann leer sein, wenn kein relevanter
     *                Kontext gefunden wurde.
     * 
     * @return Die von der KI generierte Analyse bzw. Antwort als String.
     *         Niemals null, aber kann eine Fehlermeldung enthalten, wenn die
     *         KI-Analyse fehlschlägt.
     * 
     * @throws NullPointerException wenn ticket null ist (abhängig von Implementierung)
     */
    String generateAnalysis(SupportTicket ticket, String context);

    /** Verarbeitet eine direkte Chat-Anfrage (neu für WebSockets). */
    String generateChatResponse(String userPrompt, String context);
}