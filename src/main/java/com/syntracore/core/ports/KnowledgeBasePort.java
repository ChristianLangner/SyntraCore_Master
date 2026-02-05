package com.syntracore.core.ports;

import java.util.List;

/**
 * Port-Schnittstelle für den Zugriff auf die Wissensdatenbank (Knowledge Base).
 * 
 * <p>Dieses Interface ist ein <strong>Outbound-Port</strong> in der Hexagonalen Architektur
 * und ermöglicht der Domain-Schicht den Zugriff auf firmenspezifisches Wissen für die
 * <strong>RAG-basierte KI-Analyse</strong> (Retrieval-Augmented Generation).</p>
 * 
 * <h2>Warum ist dieser Port wichtig?</h2>
 * <ul>
 *   <li><strong>RAG-Unterstützung:</strong> Ermöglicht es der KI, auf firmenspezifisches
 *       Wissen zuzugreifen, anstatt nur auf ihr Trainings-Wissen angewiesen zu sein.</li>
 *   <li><strong>Qualitätsverbesserung:</strong> KI-Antworten werden präziser und relevanter,
 *       da sie auf aktuellen Handbüchern und Dokumentationen basieren.</li>
 *   <li><strong>Flexibilität:</strong> Die Implementierung kann verschiedene Datenquellen
 *       nutzen (Vektordatenbank, Volltextsuche, Elasticsearch, etc.).</li>
 *   <li><strong>Testbarkeit:</strong> Kann im Test durch Mock-Daten ersetzt werden.</li>
 * </ul>
 * 
 * <h2>Architektur-Kontext:</h2>
 * <pre>
 * Domain Layer (definiert Port) ← Adapter Layer (implementiert Port)
 * 
 * Beispiel:
 * KnowledgeBasePort (Interface) ← MockKnowledgeAdapter (Implementierung)
 *                                ← VectorDatabaseAdapter (zukünftig)
 * </pre>
 * 
 * <h2>RAG-Workflow:</h2>
 * <ol>
 *   <li>Ticket-Nachricht wird analysiert</li>
 *   <li>Relevante Textabschnitte werden aus der Wissensdatenbank abgerufen (dieser Port)</li>
 *   <li>Kontext wird an die KI übergeben (via {@link AiServicePort})</li>
 *   <li>KI generiert Antwort basierend auf Ticket + Kontext</li>
 * </ol>
 * 
 * @author SyntraCore Development Team
 * @version 2.0
 * @since 2.0
 * 
 * @see com.syntracore.core.ports.AiServicePort
 * @see com.syntracore.adapters.outbound.database.MockKnowledgeAdapter
 */
public interface KnowledgeBasePort {

    /**
     * Sucht nach relevanten Informationen in der Wissensdatenbank basierend auf
     * der Ticket-Nachricht.
     * 
     * <p>Die Implementierung kann verschiedene Suchmethoden verwenden:</p>
     * <ul>
     *   <li><strong>Vektorsuche:</strong> Semantische Ähnlichkeit via Embeddings</li>
     *   <li><strong>Volltextsuche:</strong> Keyword-basierte Suche</li>
     *   <li><strong>Hybrid:</strong> Kombination aus beiden Ansätzen</li>
     * </ul>
     * 
     * <p><strong>Verwendungsbeispiel:</strong></p>
     * <pre>
     * String query = "Passwort vergessen";
     * List&lt;String&gt; context = knowledgeBase.findRelevantContext(query);
     * // Ergebnis: ["HANDBUCH: Passwort-Reset über Portal möglich", ...]
     * </pre>
     * 
     * @param query Die Nachricht des Kunden, nach der gesucht werden soll.
     *              Typischerweise die {@link com.syntracore.core.domain.SupportTicket#getMessage()}
     *              des Tickets. Darf nicht null sein.
     * 
     * @return Eine Liste mit passenden Textabschnitten aus dem Handbuch oder der
     *         Wissensdatenbank. Die Liste ist niemals null, kann aber leer sein,
     *         wenn keine relevanten Informationen gefunden wurden. Die Ergebnisse
     *         sind nach Relevanz sortiert (relevanteste zuerst).
     * 
     * @throws NullPointerException wenn query null ist (abhängig von Implementierung)
     */
    List<String> findRelevantContext(String query);
}