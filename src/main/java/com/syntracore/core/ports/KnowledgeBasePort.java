package com.syntracore.core.ports;

import com.syntracore.core.domain.KnowledgeEntry;
import java.util.List;
import java.util.UUID;

/**
 * Outbound Port – Driven Port für die Wissensdatenbank-Integration.
 * <p>
 * Verantwortlich für die Retrieval-Phase im RAG-Workflow. Ermöglicht die
 * semantische Suche nach relevantem Kontext für KI-Analysen und Chat-Antworten.
 * Unterstützt sowohl vektor-basierte als auch traditionelle Datenbankabfragen.
 * </p>
 *
 * @see Outbound-Port gemäß hexagonaler Architektur
 * @author Christian Langner
 * @version 2.0
 * @since 2026
 */
public interface KnowledgeBasePort {

    /**
     * Sucht nach relevantem Kontext aus der Wissensdatenbank.
     * Kernoperation der Retrieval-Phase im RAG-Workflow.
     *
     * @param query Suchbegriff oder Anfrage zur Kontext-Extraktion
     * @return Liste der relevanten Kontextausschnitte, maximal 3 Elemente
     */
    List<String> findRelevantContext(String query, UUID customerId);

    /**
     * Persistiert einen neuen Wissenseintrag in der Datenbank.
     * Unterstützt verschiedene Backends (relational, vektor-basiert).
     *
     * @param entry Der zu speichernde Wissenseintrag
     * @return Gespeicherter Eintrag mit generierter ID
     */
    KnowledgeEntry save(KnowledgeEntry entry);

    /**
     * Liefert alle vorhandenen Wissenseinträge.
     * Hauptsächlich für Admin-Zwecke verwendet.
     *
     * @return Liste aller KnowledgeEntry-Einträge
     */
    List<KnowledgeEntry> findAll();
}