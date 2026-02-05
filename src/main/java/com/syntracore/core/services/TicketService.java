// UPDATE #33: TicketService mit korrekter getAllTickets Methode
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
public class TicketService implements TicketUseCase {

    private final TicketRepositoryPort ticketRepository;
    private final KnowledgeBasePort knowledgeBase;
    private final AiServicePort aiService;

    @Override
    public void createAndProcessTicket(String customerName, String message) {
        SupportTicket ticket = new SupportTicket(customerName, message);
        List<String> context = knowledgeBase.findRelevantContext(message);
        String combinedContext = String.join("\n", context);
        String analysis = aiService.generateAnalysis(ticket, combinedContext);
        ticket.setAiAnalysis(analysis);
        ticketRepository.save(ticket);
    }

    @Override
    public String processInquiry(String userMessage) {
        List<String> context = knowledgeBase.findRelevantContext(userMessage);
        return aiService.generateChatResponse(userMessage, String.join("\n", context));
    }

    // Diese Methode wird vom AdminController gesucht
    public List<SupportTicket> getAllTickets() {
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
            ticket.setResolved(true);
            ticketRepository.save(ticket);
        });
    }
}