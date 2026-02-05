package com.syntracore.adapters.outbound.database;

// JPA-Annotationen für die Abbildung auf die Datenbank
import jakarta.persistence.*;
// Zeitstempel für Erstellungsdatum
import java.time.LocalDateTime;
// Eindeutige ID für das Ticket
import java.util.UUID;
// Lombok: Generiert automatisch Getter- und Setter-Methoden zur Compile-Zeit
import lombok.Getter;
import lombok.Setter;

/**
 * JPA-Entität für die Persistierung von Support-Tickets in der Datenbank.
 * 
 * <p>Diese Klasse ist Teil des <strong>Adapter-Layers</strong> in der Hexagonalen Architektur
 * und repräsentiert die <strong>Datenbank-Sicht</strong> eines Support-Tickets. Sie ist
 * bewusst vom Domain-Modell ({@link com.syntracore.core.domain.SupportTicket}) getrennt.</p>
 * 
 * <h2>Warum ist diese Trennung wichtig?</h2>
 * <ul>
 *   <li><strong>Unabhängigkeit:</strong> Das Domain-Modell bleibt frei von JPA-Annotationen
 *       und Datenbank-Abhängigkeiten.</li>
 *   <li><strong>Flexibilität:</strong> Datenbank-Schema kann sich ändern, ohne die Domain
 *       zu beeinflussen (z.B. Spalten umbenennen, Normalisierung).</li>
 *   <li><strong>Klarheit:</strong> Klare Trennung zwischen fachlicher Logik (Domain) und
 *       technischer Persistierung (JPA-Entität).</li>
 * </ul>
 * 
 * <h2>Architektur-Kontext:</h2>
 * <pre>
 * Domain-Modell (SupportTicket) ← Adapter übersetzt → JPA-Entität (hier) → Datenbank
 * </pre>
 * 
 * <h2>Technische Details:</h2>
 * <ul>
 *   <li><strong>Tabelle:</strong> {@code tickets} in der Datenbank</li>
 *   <li><strong>Primärschlüssel:</strong> UUID (keine Auto-Increment-ID)</li>
 *   <li><strong>Lombok:</strong> Getter/Setter werden automatisch generiert</li>
 * </ul>
 * 
 * <h2>Datenbank-Schema:</h2>
 * <pre>
 * CREATE TABLE tickets (
 *     id           UUID PRIMARY KEY,
 *     customer_name VARCHAR(255),
 *     message      VARCHAR(255),
 *     created_at   TIMESTAMP,
 *     ai_analysis  TEXT
 * );
 * </pre>
 * 
 * @author SyntraCore Development Team
 * @version 2.0
 * @since 1.0
 * 
 * @see com.syntracore.core.domain.SupportTicket
 * @see TicketDatabaseAdapter
 * @see SpringDataTicketRepository
 */
@Entity                             // Markiert diese Klasse als JPA-Entität
@Table(name = "tickets")           // Verknüpft die Entität mit der Tabelle "tickets"
@Getter                            // Lombok: erzeugt Getter für alle Felder
@Setter                            // Lombok: erzeugt Setter für alle Felder
public class TicketJpaEntity {

    /**
     * Primärschlüssel des Tickets.
     * 
     * <p>Verwendet UUID statt Long/Integer für folgende Vorteile:</p>
     * <ul>
     *   <li><strong>Verteilte Systeme:</strong> Keine Konflikte bei Replikation</li>
     *   <li><strong>Sicherheit:</strong> IDs sind nicht vorhersagbar (kein Enumeration-Angriff)</li>
     *   <li><strong>Offline-Erstellung:</strong> IDs können ohne Datenbankzugriff generiert werden</li>
     * </ul>
     * 
     * <p><strong>Hinweis:</strong> JPA erwartet keine {@code @GeneratedValue}-Annotation,
     * da die UUID bereits im Domain-Modell generiert wird.</p>
     */
    @Id
    private UUID id;

    /**
     * Name des Kunden, der das Ticket erstellt hat.
     * 
     * <p>Wird als VARCHAR(255) in der Datenbank gespeichert (Standard-Länge für String-Felder).</p>
     */
    private String customerName;

    /**
     * Nachricht bzw. Beschreibung des Problems/Anliegens.
     * 
     * <p>Enthält die ursprüngliche Anfrage des Kunden. Wird als VARCHAR(255) gespeichert.</p>
     * 
     * <p><strong>Hinweis:</strong> Für längere Nachrichten könnte {@code @Column(columnDefinition = "TEXT")}
     * verwendet werden.</p>
     */
    private String message;

    /**
     * Zeitpunkt, zu dem das Ticket erstellt wurde.
     * 
     * <p>Wird als TIMESTAMP in der Datenbank gespeichert. JPA konvertiert automatisch
     * zwischen {@link LocalDateTime} (Java) und TIMESTAMP (SQL).</p>
     */
    private LocalDateTime createdAt;

    /**
     * Das Ergebnis der KI-Analyse für dieses Ticket.
     * 
     * <p>Enthält die von der KI generierte Antwort oder Lösungsvorschläge. Dieses Feld
     * ist essentiell für den RAG-Workflow (Retrieval-Augmented Generation).</p>
     * 
     * <p><strong>Warum TEXT statt VARCHAR?</strong></p>
     * <ul>
     *   <li>KI-Antworten können sehr lang sein (mehrere Absätze)</li>
     *   <li>VARCHAR(255) würde die Antwort abschneiden</li>
     *   <li>TEXT erlaubt bis zu ~65.000 Zeichen (abhängig von Datenbank)</li>
     * </ul>
     * 
     * <p><strong>Hinweis:</strong> Kann {@code null} sein, wenn die KI-Analyse noch nicht
     * durchgeführt wurde oder fehlgeschlagen ist.</p>
     */
    @Column(columnDefinition = "TEXT")
    private String aiAnalysis;

    /**
     * Standard-Konstruktor für JPA.
     * 
     * <p>JPA benötigt einen parameterlosen Konstruktor, um Entitäten aus der Datenbank
     * zu rekonstruieren. Dieser Konstruktor sollte nicht manuell aufgerufen werden -
     * verwende stattdessen den {@link TicketDatabaseAdapter} zum Erstellen von Entitäten.</p>
     */
    public TicketJpaEntity() {}
}