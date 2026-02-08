// Autor: Christian Langner
package com.syntracore.core.ports;

import com.syntracore.core.domain.Knowledge;

import java.util.List;
import java.util.UUID;

/**
 * Outbound Port für Vektor-basierte Ähnlichkeitssuche (RAG).
 *
 * <p><strong>Architektur-Schicht:</strong> Port (Hexagonal Core)</p>
 * <p><strong>Framework-Unabhängigkeit:</strong> Keine Spring/JPA/pgvector-Abhängigkeiten</p>
 *
 * <h2>Hexagonale Architektur:</h2>
 * <p>Dieser Port definiert die Schnittstelle für semantische Suche mit Vektor-Embeddings.
 * Die konkrete Implementierung erfolgt durch einen Adapter (z.B. PostgreSQL mit pgvector).</p>
 *
 * <h2>RAG-Workflow (Retrieval-Augmented Generation):</h2>
 * <ol>
 *   <li><strong>Retrieval:</strong> Diese Schnittstelle findet relevante Wissenseinträge</li>
 *   <li><strong>Augmentation:</strong> Die gefundenen Einträge werden als Kontext hinzugefügt</li>
 *   <li><strong>Generation:</strong> Die KI generiert Antworten basierend auf dem Kontext</li>
 * </ol>
 *
 * <h2>Verantwortlichkeiten:</h2>
 * <ul>
 *   <li><strong>Semantische Suche:</strong> Findet ähnliche Inhalte via Vektor-Ähnlichkeit</li>
 *   <li><strong>Mandantenfähigkeit:</strong> Berücksichtigt Company-ID für Multi-Tenancy</li>
 *   <li><strong>Ranking:</strong> Sortiert Ergebnisse nach Ähnlichkeit</li>
 * </ul>
 *
 * @author Christian Langner
 * @version 1.0
 * @since 2026
 *
 * @see Knowledge
 */
public interface VectorSearchPort {

    /**
     * Findet ähnlichen Kontext für einen gegebenen Query-Text.
     *
     * <p>Der Query-Text wird zunächst in ein Embedding umgewandelt, dann werden
     * die ähnlichsten Wissenseinträge aus der Datenbank abgerufen.</p>
     *
     * @param queryText Der Suchtext (z.B. User-Frage oder Ticket-Inhalt)
     * @param companyId Die Company-ID für Multi-Tenancy-Filterung
     * @param limit Maximale Anzahl der zurückzugebenden Ergebnisse
     * @return Liste der ähnlichsten Knowledge-Einträge, sortiert nach Relevanz
     */
    List<Knowledge> findSimilarContext(String queryText, UUID companyId, int limit);

    /**
     * Findet ähnlichen Kontext anhand eines bereits vorhandenen Embeddings.
     *
     * <p>Nützlich, wenn das Embedding bereits berechnet wurde und
     * wiederverwendet werden soll.</p>
     *
     * @param embedding Das Query-Embedding (z.B. von OpenAI)
     * @param companyId Die Company-ID für Multi-Tenancy-Filterung
     * @param limit Maximale Anzahl der zurückzugebenden Ergebnisse
     * @return Liste der ähnlichsten Knowledge-Einträge, sortiert nach Relevanz
     */
    List<Knowledge> findSimilarContextByEmbedding(float[] embedding, UUID companyId, int limit);

    /**
     * Findet ähnlichen Kontext mit zusätzlichem Similarity-Score.
     *
     * @param queryText Der Suchtext
     * @param companyId Die Company-ID für Multi-Tenancy-Filterung
     * @param limit Maximale Anzahl der zurückzugebenden Ergebnisse
     * @param minSimilarity Minimaler Ähnlichkeits-Score (0.0 - 1.0)
     * @return Liste von ScoredKnowledge mit Ähnlichkeits-Score
     */
    List<ScoredKnowledge> findSimilarContextWithScore(
            String queryText,
            UUID companyId,
            int limit,
            double minSimilarity
    );

    /**
     * Speichert einen Knowledge-Eintrag mit Embedding.
     *
     * <p>Das Embedding wird automatisch generiert, falls nicht vorhanden.</p>
     *
     * @param knowledge Der zu speichernde Knowledge-Eintrag
     * @return Der gespeicherte Knowledge-Eintrag mit generiertem Embedding
     */
    Knowledge saveWithEmbedding(Knowledge knowledge);

    /**
     * Knowledge-Eintrag mit Ähnlichkeits-Score.
     */
    record ScoredKnowledge(
            Knowledge knowledge,
            double similarity
    ) {
        /**
         * Prüft, ob die Ähnlichkeit über dem gegebenen Schwellwert liegt.
         *
         * @param threshold Schwellwert (0.0 - 1.0)
         * @return true, wenn ähnlich genug
         */
        public boolean isRelevant(double threshold) {
            return similarity >= threshold;
        }
    }
}
