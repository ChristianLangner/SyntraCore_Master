package com.syntracore.core.ports;

import java.util.List;

/**
 * Port für den Zugriff auf die Wissensdatenbank.
 * Erlaubt es dem Core-Service, Informationen für die KI-Analyse zu finden.
 */
public interface KnowledgeBasePort {

    /**
     * Sucht nach relevanten Informationen basierend auf dem Ticket-Inhalt.
     * @param query Die Nachricht des Kunden.
     * @return Eine Liste mit passenden Textabschnitten aus dem Handbuch.
     */
    List<String> findRelevantContext(String query);
}