// Autor: Christian Langner
package com.ayntracore.core.ports;

import com.ayntracore.core.domain.KnowledgeEntry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Definiert den Port für den Zugriff auf die Wissensdatenbank.
 * Dieser Port wird von Adaptern für verschiedene Datenquellen implementiert.
 */
public interface KnowledgeBasePort {

    /**
     * Findet relevanten Kontext basierend auf einer Suchanfrage und Mandanten-ID.
     *
     * @param query Die Suchanfrage
     * @param companyId Die ID des Mandanten
     * @return Eine Liste von relevanten Inhalten
     */
    List<String> findRelevantContext(String query, UUID companyId);

    /**
     * Findet einen Wissenseintrag anhand seiner ID und Mandanten-ID.
     *
     * @param id Die ID des Wissenseintrags
     * @param companyId Die ID des Mandanten
     * @return Ein Optional, das den Wissenseintrag enthält, wenn gefunden
     */
    Optional<KnowledgeEntry> findById(UUID id, UUID companyId);

    /**
     * Findet relevante Wissenseinträge basierend auf Suchkriterien.
     *
     * @param query Die Suchanfrage
     * @param companyId Die ID des Mandanten
     * @param category Die Kategorie (optional)
     * @return Eine Liste passender Wissenseinträge
     */
    List<KnowledgeEntry> findRelevantEntries(String query, UUID companyId, String category);

    /**
     * Speichert einen neuen oder aktualisiert einen bestehenden Wissenseintrag.
     *
     * @param entry Der zu speichernde Wissenseintrag
     * @return Der gespeicherte Wissenseintrag
     */
    KnowledgeEntry save(KnowledgeEntry entry);

    /**
     * Ruft alle Wissenseinträge ab.
     *
     * @return Eine Liste aller Wissenseinträge
     */
    List<KnowledgeEntry> findAll();
}
