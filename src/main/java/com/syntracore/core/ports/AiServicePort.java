package com.syntracore.core.ports;

import com.syntracore.core.domain.SupportTicket;

/**
 * Outbound Port – Driven Port für KI-basierte Ticket-Analyse.
 */
public interface AiServicePort {

    String generateAnalysis(SupportTicket ticket, String context, String systemPrompt, String speakingStyle);

    String generateChatResponse(String userPrompt, String context, String systemPrompt, String speakingStyle);

    // ... existing code ...
}