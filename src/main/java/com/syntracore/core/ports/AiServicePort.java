package com.syntracore.core.ports;

import com.syntracore.core.domain.SupportTicket;

/**
 * Port für KI-Dienste.
 */
public interface AiServicePort {
    /**
     * Generiert eine Analyse unter Berücksichtigung von zusätzlichem Wissen (RAG).
     * @param ticket Das zu analysierende Ticket.
     * @param context Der gefundene Text aus der Wissensdatenbank.
     * @return Die Antwort der KI.
     */
    String generateAnalysis(SupportTicket ticket, String context);
}