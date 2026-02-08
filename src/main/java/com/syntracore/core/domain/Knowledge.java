// Autor: Christian Langner
package com.syntracore.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Domain-Modell für Wissenseinträge mit Vektor-Embedding-Support.
 *
 * <p><strong>Architektur-Schicht:</strong> Domain-Modell (Hexagonal Core)</p>
 * <p><strong>Framework-Unabhängigkeit:</strong> Keine Spring/JPA/pgvector-Abhängigkeiten</p>
 *
 * <h2>Verwendung im RAG-Workflow:</h2>
 * <ul>
 *   <li><strong>Retrieval:</strong> Semantische Suche mittels Vector-Embeddings</li>
 *   <li><strong>Augmentation:</strong> Anreicherung der Ticket-Daten mit Fachkontext</li>
 *   <li><strong>Generation:</strong> KI-gestützte Antworten mit aktuellem Wissen</li>
 * </ul>
 *
 * <h2>Embedding-Abstraktion:</h2>
 * <p>Das Embedding wird als float[] repräsentiert, um die Domain-Schicht von
 * technischen Implementierungsdetails (z.B. pgvector) zu entkoppeln.
 * Die Konvertierung erfolgt in den Adaptern.</p>
 *
 * <h2>Clean Code Prinzipien:</h2>
 * <ul>
 *   <li><strong>Single Responsibility:</strong> Nur Datenhaltung für Wissenseinträge</li>
 *   <li><strong>Framework-Unabhängigkeit:</strong> Keine technischen Annotationen</li>
 *   <li><strong>Mandantenfähigkeit:</strong> CompanyId für Multi-Tenancy</li>
 * </ul>
 *
 * @author Christian Langner
 * @version 2.0
 * @since 2026
 *
 * @see KnowledgeEntry (alte Version, wird durch Knowledge ersetzt)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Knowledge {

    /** Eindeutige UUID des Wissenseintrags */
    private UUID id;

    /** Mandanten-ID für Multi-Tenancy */
    private UUID companyId;

    /** Inhalt des Wissenseintrags */
    private String content;

    /** Kategorie für thematische Gruppierung */
    private String category;

    /** Herkunftsquelle des Wissenseintrags */
    private String source;

    /**
     * Vektor-Embedding für semantische Suche (1536 Dimensionen).
     * Wird von OpenAI text-embedding-3-small generiert.
     * Im Domain-Modell als float[] repräsentiert (technologie-agnostisch).
     */
    private float[] embedding;

    /**
     * Konstruktor für neue Wissenseinträge ohne Embedding.
     * Das Embedding wird später vom EmbeddingService hinzugefügt.
     *
     * @param companyId Mandanten-ID
     * @param content Inhalt des Wissenseintrags
     * @param category Kategorie
     * @param source Herkunftsquelle
     */
    public Knowledge(UUID companyId, String content, String category, String source) {
        this.id = UUID.randomUUID();
        this.companyId = companyId;
        this.content = content;
        this.category = category;
        this.source = source;
        this.embedding = null;
    }

    /**
     * Prüft, ob ein Embedding vorhanden ist.
     *
     * @return true, wenn Embedding gesetzt ist
     */
    public boolean hasEmbedding() {
        return embedding != null && embedding.length > 0;
    }
}
