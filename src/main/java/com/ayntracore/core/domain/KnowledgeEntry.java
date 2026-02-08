// Autor: Christian Langner
package com.ayntracore.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * Domain-Modell für Wissenseinträge gemäß hexagonaler Architektur.
 * Repräsentiert das fachliche Kernmodell ohne technische Abhängigkeiten.
 * 
 * <p><strong>Architektur-Schicht:</strong> Domain-Modell</p>
 * <p><strong>Zweck:</strong> Enthält die fachlichen Daten für Wissenseinträge,
 * die im RAG-Workflow zur KI-gestützten Ticket-Analyse verwendet werden.</p>
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
 *   <li><strong>Single Responsibility:</strong> Nur Datenhaltung für Wissenseinträge</li>
 *   <li><strong>Framework-Unabhängigkeit:</strong> Keine Spring/JPA-Annotationen</li>
 *   <li><strong>Flexible Konstruktion:</strong> Mehrere Konstruktoren für verschiedene Use-Cases</li>
 *   <li><strong>Selbstdokumentierend:</strong> Klare, sprechende Feldnamen</li>
 * </ul>
 * 
 * <h2>Verwendung im RAG-Workflow:</h2>
 * <p>Diese Domain-Klasse wird verwendet für:</p>
 * <ul>
 *   <li><strong>Retrieval:</strong> Semantische Suche mittels Vector-Embeddings</li>
 *   <li><strong>Augmentation:</strong> Anreicherung der Ticket-Daten mit Fachkontext</li>
 *   <li><strong>Generation:</strong> KI-gestützte Antworten mit aktuellem Wissen</li>
 * </ul>
 * 
 * @author Christian Langner
 * @version 2.0
 * @since 2026
 * 
 * @see com.ayntracore.core.domain.SupportTicket
 * @see com.ayntracore.core.services.TicketService
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeEntry {
    
    /** Eindeutige UUID des Wissenseintrags (Primärschlüssel) */
    private UUID id;
    
    /** Inhalt des Wissenseintrags */
    private String content;
    
    /** Herkunftsquelle des Wissenseintrags */
    private String source;
    
    /** Kategorie für thematische Gruppierung */
    private String category;

    /** NEU: Mandanten-Bezug im Domain-Modell */
    private UUID companyId;

    /**
     * Konstruktor für neue Wissenseinträge vom Admin-System.
     * 
     * @param category Kategorie des Wissenseintrags
     * @param content Inhalt des Wissenseintrags
     * ÄNDERUNG: companyId als Pflichtfeld hinzugefügt.
     */
    public KnowledgeEntry(String category, String content, UUID companyId) {
        this.id = UUID.randomUUID();
        this.category = category;
        this.content = content;
        this.source = "Admin Ingest";
        this.companyId = companyId;
    }
}
