package com.syntracore.core.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain-Modell für ein Support-Ticket im SyntraCore-System.
 * 
 * <p>Diese Klasse repräsentiert die <strong>fachliche Kernlogik</strong> eines Support-Tickets
 * und ist das zentrale Geschäftsobjekt der Anwendung. Sie folgt den Prinzipien der
 * <strong>Hexagonalen Architektur (Ports & Adapters)</strong> und ist bewusst
 * <strong>framework-unabhängig</strong> gestaltet.</p>
 * 
 * <h2>Warum ist diese Klasse wichtig?</h2>
 * <ul>
 *   <li><strong>Unabhängigkeit:</strong> Keine Abhängigkeiten zu Spring, JPA oder anderen Frameworks.
 *       Die Business-Logik bleibt rein und testbar.</li>
 *   <li><strong>Wiederverwendbarkeit:</strong> Kann in verschiedenen Kontexten (Web, CLI, Tests)
 *       ohne Änderungen verwendet werden.</li>
 *   <li><strong>Wartbarkeit:</strong> Änderungen an der Datenbank oder am Framework
 *       beeinflussen diese Klasse nicht.</li>
 * </ul>
 * 
 * <h2>Architektur-Kontext:</h2>
 * <pre>
 * Domain Layer (hier) ← Service Layer ← Adapter Layer
 * </pre>
 * 
 * <p>Die Domain-Schicht kennt weder Services noch Adapter. Sie definiert nur,
 * <strong>was</strong> ein Ticket ist, nicht <strong>wie</strong> es gespeichert oder verarbeitet wird.</p>
 * 
 * @author SyntraCore Development Team
 * @version 2.0
 * @since 1.0
 * 
 * @see com.syntracore.core.ports.TicketRepositoryPort
 * @see com.syntracore.core.services.TicketService
 */
public class SupportTicket {

    /**
     * Eindeutige Ticket-ID.
     * 
     * <p>Wird beim Erzeugen automatisch als UUID generiert. UUIDs garantieren
     * Eindeutigkeit auch in verteilten Systemen ohne zentrale ID-Vergabe.</p>
     * 
     * <p><strong>Warum UUID statt Long?</strong> UUIDs vermeiden Konflikte bei
     * Replikation und ermöglichen Offline-Erstellung von Tickets.</p>
     */
    private UUID id;

    /**
     * Name der Person, die das Ticket erstellt hat.
     * 
     * <p>Wird für die Zuordnung und Kommunikation mit dem Kunden verwendet.</p>
     */
    private String customerName;

    /**
     * Beschreibung bzw. Nachricht des Tickets.
     * 
     * <p>Enthält das Problem oder die Anfrage des Kunden. Dieser Text wird
     * an die KI-Analyse weitergegeben.</p>
     */
    private String message;

    /**
     * Zeitpunkt der Erstellung des Tickets.
     * 
     * <p>Wird automatisch beim Konstruktor-Aufruf auf die aktuelle Zeit gesetzt.
     * Wichtig für Priorisierung und SLA-Tracking.</p>
     */
    private LocalDateTime createdAt;

    /**
     * Ergebnis der KI-Analyse zu diesem Ticket.
     * 
     * <p>Wird erst nach der Ticket-Erstellung durch den Service gesetzt.
     * Enthält die automatisch generierte Antwort oder Lösungsvorschläge der KI.</p>
     * 
     * <p><strong>Hinweis:</strong> Kann {@code null} sein, wenn die KI-Analyse
     * noch nicht durchgeführt wurde oder fehlgeschlagen ist.</p>
     */
    private String aiAnalysis;

    /**
     * Erzeugt ein neues Support-Ticket mit Kundenname und Nachricht.
     * 
     * <p>Die Ticket-ID wird automatisch als UUID generiert, und der Erstellungszeitpunkt
     * wird auf die aktuelle Systemzeit gesetzt.</p>
     * 
     * <p><strong>Verwendungsbeispiel:</strong></p>
     * <pre>
     * SupportTicket ticket = new SupportTicket("Max Mustermann", "Login funktioniert nicht");
     * </pre>
     * 
     * @param customerName Der Name des Kunden, der das Ticket erstellt (darf nicht null sein)
     * @param message Die Problembeschreibung oder Anfrage des Kunden (darf nicht null sein)
     * 
     * @throws NullPointerException wenn customerName oder message null sind
     *                              (implizit durch spätere Verwendung)
     */
    public SupportTicket(String customerName, String message) {
        this.id = UUID.randomUUID();
        this.customerName = customerName;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Gibt die eindeutige ID des Tickets zurück.
     * 
     * @return Die UUID des Tickets (niemals null)
     */
    public UUID getId() { 
        return id; 
    }

    /**
     * Gibt den Namen des Kunden zurück, der das Ticket erstellt hat.
     * 
     * @return Der Kundenname (niemals null)
     */
    public String getCustomerName() { 
        return customerName; 
    }

    /**
     * Gibt die Nachricht bzw. Problembeschreibung des Tickets zurück.
     * 
     * @return Die Ticket-Nachricht (niemals null)
     */
    public String getMessage() { 
        return message; 
    }

    /**
     * Gibt den Erstellungszeitpunkt des Tickets zurück.
     * 
     * @return Der Zeitpunkt der Ticket-Erstellung (niemals null)
     */
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }

    /**
     * Gibt das Ergebnis der KI-Analyse zurück.
     * 
     * @return Die KI-Analyse als String, oder {@code null} wenn noch nicht durchgeführt
     */
    public String getAiAnalysis() { 
        return aiAnalysis; 
    }

    /**
     * Setzt das KI-Analyse-Ergebnis für dieses Ticket.
     * 
     * <p>Diese Methode wird vom {@link com.syntracore.core.services.TicketService}
     * aufgerufen, nachdem die KI-Analyse abgeschlossen wurde.</p>
     * 
     * <p><strong>Hinweis:</strong> Dies ist die einzige Setter-Methode in dieser Klasse,
     * da alle anderen Felder nach der Erstellung unveränderlich (immutable) bleiben sollen.</p>
     * 
     * @param aiAnalysis Das Ergebnis der KI-Analyse (kann null sein bei Fehler)
     */
    public void setAiAnalysis(String aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
    }
}