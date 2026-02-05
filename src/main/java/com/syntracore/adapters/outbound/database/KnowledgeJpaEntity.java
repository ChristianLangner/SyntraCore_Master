package com.syntracore.adapters.outbound.database;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

/**
 * JPA-Entity für Wissenseinträge in der Wissensbasis-Tabelle.
 * <p>
 * Repräsentiert die physikalische Datenbankstruktur für Wissenseinträge.
 * Übersetzt Domain-Modelle zu Datenbank-Entities gemäß hexagonaler Architektur.
 * </p>
 * 
 * <p><strong>Architektur-Schicht:</strong> Outbound-Adapterschicht (Technische Implementierung)</p>
 * <p><strong>Zweck:</strong> Persistierungsschicht für Wissenseinträge mittels JPA/Hibernate</p>
 * 
 * <h2>Datenbankstruktur:</h2>
 * <ul>
 *   <li><strong>Tabelle:</strong> knowledge_base</li>
 *   <li><strong>Primärschlüssel:</strong> UUID (GenerationType.AUTO)</li>
 *   <li><strong>Constraints:</strong> TEXT für lange Inhalte, String für Kategorien/Quellen</li>
 * </ul>
 * 
 * <h2>UUID als Primärschlüssel:</h2>
 * <ul>
 *   <li><strong>Cloud-Kompatibilität:</strong> Keine Sequenz-Konflikte in verteilten Systemen</li>
 *   <li><strong>Sicherheit:</strong> Nicht vorhersagbar wie sequentielle IDs</li>
 *   <li><strong>Unabhängigkeit:</strong> Generierung ohne Datenbank-Zugriff möglich</li>
 * </ul>
 * 
 * @author Christian Langner
 * @version 2.0
 * @since 2026
 * 
 * @see com.syntracore.core.domain.KnowledgeEntry
 * @see com.syntracore.adapters.outbound.database.DatabaseKnowledgeAdapter
 */
@Entity
@Table(name = "knowledge_base")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class KnowledgeJpaEntity {

    /**
     * Technischer Primärschlüssel als UUID.
     * Automatische Generierung durch Hibernate/Datenbank.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Inhalt des Wissenseintrags als TEXT-Datentyp.
     * Unterstützt lange Dokumente wie Handbuch-Fragmente.
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * Kategorie für thematische Gruppierung.
     * Beispiele: Installation, Login, Konfiguration, Fehlerbehebung.
     */
    private String category;

    /**
     * Quelle des Wissenseintrags.
     * Dokumentiert Herkunft zur Nachverfolgbarkeit.
     */
    private String source;
}