// Autor: Christian Langner
package com.ayntracore.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * Domain-Modell für Wissenseinträge gemäß hexagonaler Architektur.
 * Repräsentiert das fachliche Kernmodell ohne technische Abhängigkeiten.
 *
 * @author Christian Langner
 * @version 2.1
 * @since 2026
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KnowledgeEntry {
    
    /** Eindeutige UUID des Wissenseintrags (Primärschlüssel) */
    private UUID id;
    
    /** Inhalt des Wissenseintrags */
    private String content;
    
    /** Herkunftsquelle des Wissenseintrags */
    private String source;
    
    /** Kategorie für thematische Gruppierung */
    private String category;

    /** Mandanten-Bezug im Domain-Modell */
    private UUID companyId;

    /** Vektor-Embedding für die semantische Suche */
    private float[] embedding;

    /**
     * Konstruktor für neue Wissenseinträge vom Admin-System.
     * 
     * @param category Kategorie des Wissenseintrags
     * @param content Inhalt des Wissenseintrags
     * @param companyId Mandanten-ID
     */
    public KnowledgeEntry(String category, String content, UUID companyId) {
        this.id = UUID.randomUUID();
        this.category = category;
        this.content = content;
        this.source = "Admin Ingest";
        this.companyId = companyId;
        // Das Embedding wird absichtlich null gelassen, da es später generiert wird.
    }
}
