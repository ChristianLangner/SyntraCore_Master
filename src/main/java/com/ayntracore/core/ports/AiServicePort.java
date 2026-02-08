// Autor: Christian Langner
package com.ayntracore.core.ports;

import com.ayntracore.core.domain.Persona;
import com.ayntracore.core.domain.SupportTicket;

/**
 * // UPDATE #58
 * Outbound Port – Driven Port für KI-basierte Ticket-Analyse.
 */
public interface AiServicePort {

    String generateAnalysis(SupportTicket ticket, String context, Persona persona);

    String generateChatResponse(String userPrompt, String context, Persona persona);

    // ... existing code ...
}
