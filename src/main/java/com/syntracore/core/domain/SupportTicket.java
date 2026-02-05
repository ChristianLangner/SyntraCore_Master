// UPDATE #24: TicketService an SupportTicket (UUID) angepasst
// Zweck: Zentrale Logik für Tickets und Wissens-Management
// Ort: src/main/java/com/syntracore/core/services/TicketService.java

package com.syntracore.core.services;

import com.syntracore.core.domain.KnowledgeEntry;
import com.syntracore.core.domain.SupportTicket;
import com.syntracore.core.ports.KnowledgeBasePort;
import com.syntracore.core.ports.AiServicePort;
import com.syntracore.core.ports.TicketRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepositoryPort ticketRepository;
    private final KnowledgeBasePort knowledgeBase;
    private final AiServicePort aiService; // Umbenannt von openAiPort für Port-Konsistenz

    // --- Ticket Methoden ---
    public SupportTicket createTicket(String customerName, String message) {
        SupportTicket newTicket = new SupportTicket(customerName, message);
        ticketRepository.save(newTicket);
        return newTicket;
    }

    public List<SupportTicket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public void resolveTicket(UUID ticketId) {
        ticketRepository.findById(ticketId).ifPresent(ticket -> {
            // Hier könnte man ein Status-Feld im SupportTicket ergänzen
            // Da SupportTicket aktuell kein 'resolved' Feld hat, speichern wir es einfach neu
            ticketRepository.save(ticket);
        });
    }

    // --- Wissens-Management (Admin) ---
    public KnowledgeEntry addKnowledge(KnowledgeEntry entry) {
        return knowledgeBase.save(entry);
    }

    public List<KnowledgeEntry> getAllKnowledge() {
        return knowledgeBase.findAll();
    }

    // --- RAG-Logik ---
    public String getBotResponse(String userQuery) {
        List<String> relevantContexts = knowledgeBase.findRelevantContext(userQuery);
        String context = String.join("\n", relevantContexts);

        if (context.isEmpty()) {
            return "Ich konnte dazu leider nichts in unserer Wissensdatenbank finden. Möchtest du ein Ticket erstellen?";
        }

        String prompt = "Antworte auf die Frage des Benutzers basierend auf dem folgenden Kontext:\n" +
                "Kontext: " + context + "\n" +
                "Frage: " + userQuery;

        return aiService.getCompletion(prompt);
    }
}