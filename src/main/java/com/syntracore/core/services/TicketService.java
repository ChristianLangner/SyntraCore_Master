// UPDATE #27: Finaler TicketService (UUID & SupportTicket)
// Ort: src/main/java/com/syntracore/core/services/TicketService.java

package com.syntracore.core.services;

import com.syntracore.core.domain.KnowledgeEntry;
import com.syntracore.core.domain.SupportTicket;
import com.syntracore.core.ports.KnowledgeBasePort;
import com.syntracore.core.ports.AiServicePort;
import com.syntracore.core.ports.TicketRepositoryPort;
import com.syntracore.core.ports.TicketUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService implements TicketUseCase { // Implementiert den Inbound Port!

    private final TicketRepositoryPort ticketRepository;
    private final KnowledgeBasePort knowledgeBase;
    private final AiServicePort aiService;

    @Override
    public void createAndProcessTicket(String customerName, String message) {
        SupportTicket ticket = new SupportTicket(customerName, message);

        // RAG: Kontext suchen
        List<String> context = knowledgeBase.findRelevantContext(message);
        String combinedContext = String.join("\n", context);

        // KI Analyse
        String analysis = aiService.generateAnalysis(ticket, combinedContext);
        ticket.setAiAnalysis(analysis);

        // Speichern
        ticketRepository.save(ticket);
    }

    @Override
    public String processInquiry(String userMessage) {
        List<String> context = knowledgeBase.findRelevantContext(userMessage);
        return aiService.generateChatResponse(userMessage, String.join("\n", context));
    }

    public List<SupportTicket> getOpenTickets() {
        return ticketRepository.findAll();
    }

    public KnowledgeEntry addKnowledge(KnowledgeEntry entry) {
        return knowledgeBase.save(entry);
    }

    public List<KnowledgeEntry> getAllKnowledge() {
        return knowledgeBase.findAll();
    }

    public void resolveTicket(UUID ticketId) {
        ticketRepository.findById(ticketId).ifPresent(ticket -> {
            // Hier Logik für 'gelöst' einfügen, falls gewünscht
            ticketRepository.save(ticket);
        });
    }
}