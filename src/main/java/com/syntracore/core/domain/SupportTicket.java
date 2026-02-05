package com.syntracore.core.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain-Modell für Support-Tickets gemäß hexagonaler Architektur.
 * Repräsentiert das fachliche Kernmodell ohne technische Abhängigkeiten.
 * 
 * <p><strong>Architektur-Schicht:</strong> Domain-Modell</p>
 * <p><strong>Zweck:</strong> Enthält die fachlichen Daten und Geschäftsregeln
 * für Support-Tickets, komplett unabhängig von Frameworks wie Spring oder JPA.</p>
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
 * <h2>Clean Code Prinzipien:</h2>
 * <ul>
 *   <li><strong>Single Responsibility:</strong> Nur Datenhaltung für Tickets</li>
 *   <li><strong>Framework-Unabhängigkeit:</strong> Keine Spring/JPA-Annotationen</li>
 *   <li><strong>Immutability:</strong> Felder sind nur über Konstruktor und Setter änderbar</li>
 *   <li><strong>Selbstdokumentierend:</strong> Klare, sprechende Feldnamen</li>
 * </ul>
 * 
 * @author Christian Langner
 * @version 2.0
 * @since 2026
 * 
 * @see com.syntracore.core.domain.KnowledgeEntry
 * @see com.syntracore.core.services.TicketService
 */
@Getter
@Setter
public class SupportTicket {

    /** Eindeutige UUID des Tickets (Primärschlüssel) */
    private UUID id;
    
    /** Name des Kunden */
    private String customerName;
    
    /** Beschreibung des Problems/Anfrage */
    private String message;
    
    /** Erstellungszeitpunkt des Tickets */
    private LocalDateTime createdAt;
    
    /** KI-Analyse des Problems (RAG-Ergebnis) */
    private String aiAnalysis;
    
    /** Status ob das Ticket gelöst wurde */
    private boolean resolved;

    /**
     * Konstruktor für ein neues Support-Ticket.
     * 
     * @param customerName Name des Kunden
     * @param message Beschreibung des Problems
     */
    public SupportTicket(String customerName, String message) {
        this.id = UUID.randomUUID();
        this.customerName = customerName;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.resolved = false;
    }
}