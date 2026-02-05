package com.syntracore.core.ports;

import com.syntracore.core.domain.SupportTicket;

/**
 * Port-Schnittstelle für KI-basierte Ticket-Analyse.
 * 
 * <p>Dieses Interface ist ein <strong>Outbound-Port</strong> in der Hexagonalen Architektur
 * und definiert die Anforderungen der Domain-Schicht an KI-Dienste zur automatischen
 * Analyse und Beantwortung von Support-Tickets.</p>
 * 
 * <h2>Warum ist dieser Port wichtig?</h2>
 * <ul>
 *   <li><strong>KI-Integration:</strong> Ermöglicht die Anbindung verschiedener KI-Dienste
 *       (OpenAI, Anthropic, lokale Modelle) ohne Änderung der Domain-Logik.</li>
 *   <li><strong>RAG-Unterstützung:</strong> Unterstützt Retrieval-Augmented Generation (RAG),
 *       indem Kontext aus der Wissensdatenbank an die KI übergeben wird.</li>
 *   <li><strong>Testbarkeit:</strong> Kann im Test durch Mock-Implementierungen ersetzt werden,
 *       um KI-Antworten zu simulieren ohne echte API-Calls.</li>
 *   <li><strong>Kosteneffizienz:</strong> Ermöglicht Wechsel zwischen verschiedenen KI-Anbietern
 *       je nach Kosten und Qualität.</li>
 * </ul>
 * 
 * <h2>Architektur-Kontext:</h2>
 * <pre>
 * Domain Layer (definiert Port) ← Adapter Layer (implementiert Port)
 * 
 * Beispiel:
 * AiServicePort (Interface) ← OpenAiAdapter (Implementierung)
 * </pre>
 * 
 * <h2>RAG-Konzept (Retrieval-Augmented Generation):</h2>
 * <p>Die KI erhält nicht nur die Ticket-Nachricht, sondern auch relevanten Kontext
 * aus der Wissensdatenbank. Dies verbessert die Qualität der Antworten erheblich,
 * da die KI auf firmenspezifisches Wissen zugreifen kann.</p>
 * 
 * @author SyntraCore Development Team
 * @version 2.0
 * @since 2.0
 * 
 * @see com.syntracore.core.domain.SupportTicket
 * @see com.syntracore.core.ports.KnowledgeBasePort
 * @see com.syntracore.adapters.outbound.openai.OpenAiAdapter
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
}