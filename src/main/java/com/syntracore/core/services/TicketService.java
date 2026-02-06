package com.syntracore.core.services;

import com.syntracore.core.domain.KnowledgeEntry;
import com.syntracore.core.domain.SupportTicket;
import com.syntracore.core.ports.KnowledgeBasePort;
import com.syntracore.core.ports.AiServicePort;
import com.syntracore.core.ports.TicketRepositoryPort;
import com.syntracore.core.ports.TicketUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

/**
 * Service-Klasse für die Verarbeitung von Support-Tickets mit RAG-Workflow.
 * Implementiert den Use-Case für Ticket-Verarbeitung gemäß hexagonaler Architektur.
 * 
 * <p><strong>Architektur-Schicht:</strong> Use-Case/Service-Layer (Inbound-Port)</p>
 * <p><strong>Zweck:</strong> Koordiniert den kompletten RAG-Workflow (Retrieval-Augmented Generation)
 * für Support-Tickets und Chat-Anfragen. Nutzt Ports für externe Abhängigkeiten gemäß
 * hexagonaler Architekturprinzipien.</p>
 * 
 * <h2>RAG-Workflow Technische Implementierung:</h2>
 * <p><strong>Phase 1: Retrieval</strong></p>
 * <ul>
 *   <li><strong>Semantische Suche:</strong> Vector-Embeddings für Relevance-Suche</li>
 *   <li><strong>Knowledge-Base:</strong> Zugriff auf strukturierte Wissensdatenbank</li>
 *   <li><strong>Kontext-Extraktion:</strong> Identifiziert maximal relevante Einträge</li>
 * </ul>
 * 
 * <p><strong>Phase 2: Augmentation</strong></p>
 * <ul>
 *   <li><strong>Datenanreicherung:</strong> Kombiniert Ticket-Problem mit Fachkontext</li>
 *   <li><strong>Prompt-Engineering:</strong> Strukturierte Eingabe für KI-Modelle</li>
 *   <li><strong>Kontext-Priorisierung:</strong> Sortiert Informationen nach Relevanz</li>
 * </ul>
 * 
 * <p><strong>Phase 3: Generation</strong></p>
 * <ul>
 *   <li><strong>LLM-Integration:</strong> OpenAI GPT-Modelle für Analyse</li>
 *   <li><strong>Chain-of-Thought:</strong> Schrittweise Problemanalyse</li>
 *   <li><strong>Structured Output:</strong> Konsistente Antwort-Formate</li>
 * </ul>
 * 
 * <h2>UUID als Primärschlüssel:</h2>
 * <p>Verwendung von UUIDs bietet folgende Vorteile:</p>
 * <ul>
 *   <li><strong>Cloud-Kompatibilität:</strong> Keine Sequenz-Konflikte in verteilten Systemen</li>
 *   <li><strong>Sicherheit:</strong> Nicht vorhersagbar wie sequentielle IDs</li>
 *   <li><strong>Unabhängigkeit:</strong> Generierung ohne Datenbank-Zugriff möglich</li>
 *   <li><strong>Skalierbarkeit:</strong> Ideal für Microservices-Architekturen</li>
 * </ul>
 * 
 * @author Christian Langner
 * @version 2.0
 * @since 2026
 * 
 * @see com.syntracore.core.ports.TicketUseCase
 * @see com.syntracore.core.domain.SupportTicket
 * @see com.syntracore.core.domain.KnowledgeEntry
 */
@Service
@RequiredArgsConstructor
public class TicketService implements TicketUseCase {

    private final TicketRepositoryPort ticketRepository;
    private final KnowledgeBasePort knowledgeBase;
    private final AiServicePort aiService;

    /**
     * Erstellt und verarbeitet ein Support-Ticket mit mandantenspezifischem RAG-Workflow.
     * * @param customerName Name des Kunden
     * @param message Nachricht/Beschreibung des Problems
     * @param companyId Die ID der Firma, zu der das Ticket gehört
     */
    @Override
    public void createAndProcessTicket(String customerName, String message, UUID companyId) {
        // Ticket wird mit Firmen-ID erstellt
        SupportTicket ticket = new SupportTicket(customerName, message, companyId);

        // ÄNDERUNG: Wir geben die companyId nun an den Knowledge-Port weiter,
        // damit die KI nur Firmen-eigenes Wissen als Kontext erhält.
        List<String> context = knowledgeBase.findRelevantContext(message, companyId);
        String combinedContext = String.join("\n", context);

        // Die KI analysiert das Problem basierend auf dem firmeninternen Kontext
        String analysis = aiService.generateAnalysis(ticket, combinedContext);
        ticket.setAiAnalysis(analysis);

        // Das Ticket wird inkl. Firmenzugehörigkeit gespeichert
        ticketRepository.save(ticket);
    }

    /**
     * Verarbeitet eine Chat-Anfrage mandantenspezifisch.
     * * @param userMessage Die Benutzeranfrage
     * @param companyId Die ID der Firma für den Wissens-Kontext
     * @return KI-generierte Antwort
     */
    @Override
    public String processInquiry(String userMessage, UUID companyId) {
        // ÄNDERUNG: Kontextsuche wird auf die spezifische Firma eingeschränkt
        List<String> context = knowledgeBase.findRelevantContext(userMessage, companyId);
        return aiService.generateChatResponse(userMessage, String.join("\n", context));
    }

    /**
     * Ruft Tickets ab.
     * HINWEIS: In einem professionellen System sollte hier nur nach Company gefiltert werden.
     */
    public List<SupportTicket> getAllTickets() {
        return ticketRepository.findAll();
    }

    /**
     * Fügt einen neuen Wissenseintrag hinzu.
     * Das Domain-Objekt 'entry' enthält bereits die companyId.
     */
    public KnowledgeEntry addKnowledge(KnowledgeEntry entry) {
        return knowledgeBase.save(entry);
    }

    /**
     * Ruft alle Wissenseinträge ab.
     */
    public List<KnowledgeEntry> getAllKnowledge() {
        return knowledgeBase.findAll();
    }

    /**
     * Markiert ein Ticket als gelöst.
     */
    public void resolveTicket(UUID ticketId) {
        ticketRepository.findById(ticketId).ifPresent(ticket -> {
            ticket.setResolved(true);
            ticketRepository.save(ticket);
        });
    }
}