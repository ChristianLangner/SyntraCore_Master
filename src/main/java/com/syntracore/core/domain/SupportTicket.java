package com.syntracore.core.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain-Modell für ein Support-Ticket.
 *
 * Diese Klasse enthält die fachliche Repräsentation eines Tickets
 * und ist bewusst frei von Framework-Abhängigkeiten (kein JPA, kein Spring).
 * So bleibt die Business-Logik unabhängig von der technischen Infrastruktur.
 */
public class SupportTicket {

    /**
     * Fachliche ID des Tickets.
     * Wird bei Erstellung als zufällige UUID generiert.
     */
    private UUID id;

    /**
     * Name des Kunden, der das Ticket erstellt.
     */
    private String customerName;

    /**
     * Inhalt des Tickets (Problem-/Fehlerbeschreibung).
     */
    private String message;

    /**
     * Zeitpunkt der Erstellung des Tickets.
     */
    private LocalDateTime createdAt;

    /**
     * Konstruktor, der ein neues Ticket mit Name und Nachricht erzeugt.
     * Die ID und das Erstellungsdatum werden automatisch gesetzt.
     */
    public SupportTicket(String customerName, String message) {
        this.id = UUID.randomUUID();        // Erzeugt eine neue eindeutige Ticket-ID
        this.customerName = customerName;   // Setzt den Kundennamen
        this.message = message;             // Setzt die Ticket-Nachricht
        this.createdAt = LocalDateTime.now(); // Timestamp zum Zeitpunkt der Erstellung
    }

    // Getter (Damit andere Klassen die Daten lesen können, ohne die Felder direkt zu verändern)
    public UUID getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}