package com.syntracore.core.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain-Modell für ein Support-Ticket.
 *
 * Diese Klasse beschreibt das fachliche Ticket unabhängig von
 * technischen Details wie Datenbank oder Frameworks.
 * Sie wird sowohl vom Service als auch von den Ports/Adaptern verwendet.
 */
public class SupportTicket {

    /**
     * Eindeutige Ticket-ID.
     * Wird beim Erzeugen automatisch als UUID generiert.
     */
    private UUID id;

    /**
     * Name der Person, die das Ticket erstellt hat.
     */
    private String customerName;

    /**
     * Beschreibung bzw. Nachricht des Tickets.
     */
    private String message;

    /**
     * Zeitpunkt der Erstellung des Tickets.
     */
    private LocalDateTime createdAt;

    /**
     * Ergebnis der KI-Analyse zu diesem Ticket.
     * Wird erst später im Verarbeitungsprozess gesetzt.
     */
    private String aiAnalysis;

    /**
     * Erzeugt ein neues Support-Ticket mit Name und Nachricht.
     * ID und Erstellungszeit werden automatisch gesetzt.
     */
    public SupportTicket(String customerName, String message) {
        this.id = UUID.randomUUID();
        this.customerName = customerName;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    // Nur Lesezugriff von außen (Immutable-Sicht nach außen)
    public UUID getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getAiAnalysis() { return aiAnalysis; }

    /**
     * Setzt das KI-Analyse-Ergebnis für dieses Ticket.
     */
    public void setAiAnalysis(String aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
    }
}