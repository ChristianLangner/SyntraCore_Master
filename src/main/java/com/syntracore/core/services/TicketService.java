package com.syntracore.core.services;

import com.syntracore.core.domain.SupportTicket;
import com.syntracore.core.ports.AiServicePort;
import com.syntracore.core.ports.KnowledgeBasePort;
import com.syntracore.core.ports.TicketRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service für die Verarbeitung von Support-Tickets mit KI-Unterstützung.
 * 
 * <p>Diese Klasse ist Teil der <strong>Service-Schicht</strong> (auch Application Layer genannt)
 * und orchestriert die <strong>Use Cases</strong> der Anwendung. Sie koordiniert die Zusammenarbeit
 * zwischen Domain-Modell und verschiedenen Ports/Adaptern.</p>
 * 
 * <h2>Warum ist dieser Service wichtig?</h2>
 * <ul>
 *   <li><strong>Use Case-Orchestrierung:</strong> Koordiniert den kompletten Workflow von
 *       Ticket-Erstellung über KI-Analyse bis zur Persistierung.</li>
 *   <li><strong>RAG-Implementierung:</strong> Implementiert den Retrieval-Augmented Generation
 *       Workflow durch Kombination von Wissensdatenbank und KI.</li>
 *   <li><strong>Business-Logik:</strong> Enthält die fachliche Ablauflogik, die über
 *       einfache CRUD-Operationen hinausgeht.</li>
 *   <li><strong>Transaktionssteuerung:</strong> Kann Transaktionen koordinieren (via {@code @Transactional}).</li>
 * </ul>
 * 
 * <h2>Architektur-Kontext:</h2>
 * <pre>
 * Adapter (Inbound) → Service Layer (hier) → Domain Layer + Ports → Adapter (Outbound)
 * 
 * Beispiel-Flow:
 * TicketController → TicketService → SupportTicket + Ports → Database/KI-Adapter
 * </pre>
 * 
 * <h2>RAG-Workflow (Retrieval-Augmented Generation):</h2>
 * <ol>
 *   <li><strong>Retrieval:</strong> Relevantes Wissen aus Wissensdatenbank abrufen</li>
 *   <li><strong>Augmentation:</strong> Ticket-Nachricht mit Kontext anreichern</li>
 *   <li><strong>Generation:</strong> KI generiert Antwort basierend auf beidem</li>
 * </ol>
 * 
 * <p><strong>Vorteile von RAG:</strong></p>
 * <ul>
 *   <li>KI kann auf firmenspezifisches Wissen zugreifen</li>
 *   <li>Antworten sind präziser und relevanter</li>
 *   <li>Reduziert "Halluzinationen" der KI</li>
 *   <li>Ermöglicht Antworten auf Basis aktueller Dokumentation</li>
 * </ul>
 * 
 * <p><strong>Design-Prinzipien:</strong></p>
 * <ul>
 *   <li>Service kennt nur Ports (Interfaces), keine konkreten Adapter</li>
 *   <li>Dependency Injection via Constructor (Lombok's {@code @RequiredArgsConstructor})</li>
 *   <li>Keine HTTP-, Datenbank- oder Framework-spezifischen Details</li>
 * </ul>
 * 
 * @author SyntraCore Development Team
 * @version 3.0
 * @since 1.0
 * 
 * @see com.syntracore.core.domain.SupportTicket
 * @see com.syntracore.core.ports.TicketRepositoryPort
 * @see com.syntracore.core.ports.AiServicePort
 * @see com.syntracore.core.ports.KnowledgeBasePort
 */
@Service
@RequiredArgsConstructor
public class TicketService {

    /**
     * Port für die Persistierung von Tickets.
     * 
     * <p>Wird von Spring automatisch injiziert. Die konkrete Implementierung
     * (z.B. {@link com.syntracore.adapters.outbound.database.TicketDatabaseAdapter})
     * wird zur Laufzeit bereitgestellt.</p>
     */
    private final TicketRepositoryPort ticketRepository;

    /**
     * Port für KI-basierte Ticket-Analyse.
     * 
     * <p>Wird von Spring automatisch injiziert. Die konkrete Implementierung
     * (z.B. {@link com.syntracore.adapters.outbound.openai.OpenAiAdapter})
     * wird zur Laufzeit bereitgestellt.</p>
     */
    private final AiServicePort aiService;

    /**
     * Port für den Zugriff auf die Wissensdatenbank (RAG).
     * 
     * <p>Wird von Spring automatisch injiziert. Die konkrete Implementierung
     * (z.B. {@link com.syntracore.adapters.outbound.database.MockKnowledgeAdapter})
     * wird zur Laufzeit bereitgestellt.</p>
     */
    private final KnowledgeBasePort knowledgeBase;

    /**
     * Erstellt und verarbeitet ein Support-Ticket mit vollständigem RAG-Workflow.
     * 
     * <p>Diese Methode implementiert den kompletten Use Case "Ticket erstellen und analysieren":</p>
     * <ol>
     *   <li><strong>Ticket erstellen:</strong> Domain-Objekt wird instanziiert</li>
     *   <li><strong>Wissen abrufen:</strong> Relevanter Kontext wird aus Wissensdatenbank geladen (RAG-Retrieval)</li>
     *   <li><strong>Kontext zusammenfassen:</strong> Mehrere Wissensfragmente werden kombiniert</li>
     *   <li><strong>KI-Analyse:</strong> KI generiert Antwort basierend auf Ticket + Kontext (RAG-Generation)</li>
     *   <li><strong>Ergebnis speichern:</strong> Ticket inkl. KI-Analyse wird persistiert</li>
     * </ol>
     * 
     * <h3>Ablauf-Diagramm:</h3>
     * <pre>
     * createAndProcessTicket(name, message)
     *   │
     *   ├─→ new SupportTicket(name, message)
     *   │
     *   ├─→ knowledgeBase.findRelevantContext(message)
     *   │   └─→ ["HANDBUCH-INFO: ...", "HANDBUCH-INFO: ..."]
     *   │
     *   ├─→ String.join("\n---\n", results)
     *   │   └─→ "HANDBUCH-INFO: ...\n---\nHANDBUCH-INFO: ..."
     *   │
     *   ├─→ aiService.generateAnalysis(ticket, context)
     *   │   └─→ "Basierend auf dem Handbuch empfehle ich..."
     *   │
     *   ├─→ ticket.setAiAnalysis(analysis)
     *   │
     *   └─→ ticketRepository.save(ticket)
     * </pre>
     * 
     * <h3>Verwendungsbeispiel:</h3>
     * <pre>
     * ticketService.createAndProcessTicket("Max Mustermann", "Passwort vergessen");
     * // → Ticket wird erstellt, KI analysiert mit Handbuch-Kontext, alles wird gespeichert
     * </pre>
     * 
     * <p><strong>Hinweis zur Performance:</strong> Diese Methode läuft synchron, d.h. der
     * Aufrufer wartet, bis die KI-Analyse abgeschlossen ist. Für Produktionsumgebungen
     * sollte dies asynchron erfolgen (z.B. mit {@code @Async} oder Message Queue).</p>
     * 
     * @param customerName Der Name des Kunden, der das Ticket erstellt (darf nicht null/leer sein)
     * @param message Die Problembeschreibung oder Anfrage des Kunden (darf nicht null/leer sein)
     * 
     * @throws NullPointerException wenn customerName oder message null sind
     * @throws RuntimeException bei Fehlern in der KI-Analyse oder Datenbankzugriff
     *                          (werden von den Adaptern geworfen)
     */
    public void createAndProcessTicket(String customerName, String message) {
        // Schritt 1: Domain-Modell erstellen
        // Das Ticket erhält automatisch eine UUID und einen Zeitstempel
        SupportTicket ticket = new SupportTicket(customerName, message);

        // Schritt 2: RAG-Retrieval - Wissen aus der Wissensdatenbank abrufen
        // Die Wissensdatenbank sucht nach relevanten Handbuch-Einträgen basierend auf der Nachricht
        System.out.println("🔍 Suche passendes Wissen für: " + message);
        List<String> results = knowledgeBase.findRelevantContext(message);

        // Schritt 3: Kontext zusammenfassen
        // Mehrere Wissensfragmente werden mit Trennlinien zu einem Text kombiniert
        // Beispiel: "INFO 1\n---\nINFO 2\n---\nINFO 3"
        String context = String.join("\n---\n", results);

        // Schritt 4: RAG-Generation - KI-Analyse mit Kontext anfordern
        // Die KI erhält sowohl die Ticket-Nachricht als auch den Handbuch-Kontext
        String analysis = aiService.generateAnalysis(ticket, context);

        // Schritt 5: Ergebnis im Domain-Objekt speichern
        // Das Ticket wird mit der KI-Analyse angereichert
        ticket.setAiAnalysis(analysis);

        // Schritt 6: Ticket persistent speichern
        // Das vollständige Ticket (inkl. KI-Analyse) wird in der Datenbank gespeichert
        ticketRepository.save(ticket);

        System.out.println("🚀 Service: Ticket verarbeitet (RAG aktiv).");
    }
}