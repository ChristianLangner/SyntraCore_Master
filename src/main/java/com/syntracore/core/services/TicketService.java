package com.syntracore.core.services;

import com.syntracore.core.domain.SupportTicket;
import com.syntracore.core.ports.AiServicePort;
import com.syntracore.core.ports.KnowledgeBasePort;
import com.syntracore.core.ports.TicketRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepositoryPort ticketRepository;
    private final AiServicePort aiService;
    private final KnowledgeBasePort knowledgeBase; // NEU hinzugefügt

    public void createAndProcessTicket(String customerName, String message) {
        // 1. Domain-Modell erstellen
        SupportTicket ticket = new SupportTicket(customerName, message);

        // 2. RAG: Wissen aus der Datenbank abrufen
        System.out.println("🔍 Suche passendes Wissen für: " + message);
        List<String> results = knowledgeBase.findRelevantContext(message);

        // Die Ergebnisse zu einem Textblock zusammenfassen
        String context = String.join("\n---\n", results);

        // 3. KI-Analyse mit Kontext anfordern
        String analysis = aiService.generateAnalysis(ticket, context);

        // 4. Ergebnis speichern
        ticket.setAiAnalysis(analysis);
        ticketRepository.save(ticket);

        System.out.println("🚀 Service: Ticket verarbeitet (RAG aktiv).");
    }
}