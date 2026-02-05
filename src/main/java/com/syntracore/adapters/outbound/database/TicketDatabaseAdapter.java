package com.syntracore.adapters.outbound.database;

import com.syntracore.core.domain.SupportTicket;
import com.syntracore.core.ports.TicketRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Datenbank-Adapter für die Persistierung von Support-Tickets (Outbound-Adapter).
 * 
 * <p>Diese Klasse ist ein <strong>Outbound-Adapter</strong> in der Hexagonalen Architektur
 * und implementiert das {@link TicketRepositoryPort}-Interface. Sie bildet die
 * <strong>Brücke zwischen Domain-Modell und Datenbank-Technologie</strong>.</p>
 * 
 * <h2>Warum ist dieser Adapter wichtig?</h2>
 * <ul>
 *   <li><strong>Übersetzung:</strong> Konvertiert Domain-Objekte ({@link SupportTicket})
 *       in JPA-Entitäten ({@link TicketJpaEntity}) und umgekehrt.</li>
 *   <li><strong>Entkopplung:</strong> Die Domain kennt keine JPA-Annotationen oder
 *       Datenbank-Details. Sie bleibt framework-unabhängig.</li>
 *   <li><strong>Austauschbarkeit:</strong> Die Datenbank-Technologie kann gewechselt werden
 *       (z.B. von JPA zu MongoDB) ohne die Domain zu ändern.</li>
 *   <li><strong>Testbarkeit:</strong> Im Test kann dieser Adapter durch einen Mock ersetzt
 *       werden, ohne echte Datenbank.</li>
 * </ul>
 * 
 * <h2>Architektur-Kontext:</h2>
 * <pre>
 * Domain Layer → Port (Interface) → Adapter (hier) → Datenbank
 * 
 * Beispiel:
 * SupportTicket → TicketRepositoryPort → TicketDatabaseAdapter → H2/PostgreSQL
 * </pre>
 * 
 * <h2>Mapping-Strategie:</h2>
 * <p>Der Adapter führt ein <strong>manuelles Mapping</strong> zwischen Domain-Modell
 * und JPA-Entität durch. Dies gibt volle Kontrolle über die Transformation und
 * vermeidet ungewollte Seiteneffekte durch automatisches Mapping.</p>
 * 
 * <p><strong>Alternative Ansätze:</strong></p>
 * <ul>
 *   <li>MapStruct für automatisches Mapping</li>
 *   <li>ModelMapper für Reflection-basiertes Mapping</li>
 * </ul>
 * 
 * @author SyntraCore Development Team
 * @version 2.0
 * @since 1.0
 * 
 * @see com.syntracore.core.ports.TicketRepositoryPort
 * @see com.syntracore.core.domain.SupportTicket
 * @see TicketJpaEntity
 * @see SpringDataTicketRepository
 */
@Component
@RequiredArgsConstructor
public class TicketDatabaseAdapter implements TicketRepositoryPort {

    /**
     * Spring Data JPA Repository für Datenbankoperationen.
     * 
     * <p>Wird von Spring automatisch injiziert (Constructor Injection via Lombok's
     * {@code @RequiredArgsConstructor}). Stellt CRUD-Operationen bereit, ohne dass
     * SQL-Code geschrieben werden muss.</p>
     */
    private final SpringDataTicketRepository repository;

    /**
     * Speichert ein Support-Ticket in der Datenbank.
     * 
     * <p>Diese Methode implementiert das {@link TicketRepositoryPort}-Interface und
     * führt folgende Schritte aus:</p>
     * <ol>
     *   <li>Erstellt eine neue JPA-Entität ({@link TicketJpaEntity})</li>
     *   <li>Kopiert alle Daten vom Domain-Objekt in die Entität (Mapping)</li>
     *   <li>Speichert die Entität über Spring Data JPA</li>
     *   <li>Gibt eine Bestätigung auf der Konsole aus</li>
     * </ol>
     * 
     * <p><strong>Mapping-Details:</strong></p>
     * <ul>
     *   <li>ID, Name, Nachricht, Erstellungszeit werden 1:1 übernommen</li>
     *   <li>KI-Analyse wird ebenfalls persistiert (wichtig für RAG-Workflow)</li>
     * </ul>
     * 
     * <p><strong>Verwendungsbeispiel:</strong></p>
     * <pre>
     * SupportTicket ticket = new SupportTicket("Max", "Problem XYZ");
     * ticket.setAiAnalysis("KI-Antwort...");
     * ticketDatabaseAdapter.save(ticket);
     * // → Ticket wird in DB gespeichert
     * </pre>
     * 
     * @param ticket Das zu speichernde Support-Ticket aus der Domain-Schicht
     *               (darf nicht null sein)
     * 
     * @throws NullPointerException wenn ticket null ist
     * @throws org.springframework.dao.DataAccessException bei Datenbankfehlern
     *         (z.B. Verbindung unterbrochen, Constraint-Verletzung)
     */
    @Override
    public void save(SupportTicket ticket) {
        // Schritt 1: Neue JPA-Entität erstellen
        TicketJpaEntity entity = new TicketJpaEntity();
        
        // Schritt 2: Domain-Daten in Entität mappen (manuelles Mapping)
        entity.setId(ticket.getId());
        entity.setCustomerName(ticket.getCustomerName());
        entity.setMessage(ticket.getMessage());
        entity.setCreatedAt(ticket.getCreatedAt());

        // Wichtig: KI-Analyse mit in die Entity mappen
        // Dies ist essentiell für den RAG-Workflow, damit die KI-Antwort
        // persistent gespeichert wird und später abgerufen werden kann.
        entity.setAiAnalysis(ticket.getAiAnalysis());

        // Schritt 3: Entität über Spring Data JPA speichern
        repository.save(entity);
        
        // Schritt 4: Bestätigung ausgeben (für Entwicklung/Debugging)
        System.out.println("💾 Ticket erfolgreich in der DB gespeichert: " + ticket.getId());
    }
}