// UPDATE #19: TicketService für Admin-Funktionen erweitern
// Ort: src/main/java/com/syntracore/core/services/TicketService.java

package com.syntracore.core.services;

import com.syntracore.core.domain.KnowledgeEntry;
import com.syntracore.core.domain.SupportTicket;
import com.syntracore.core.ports.KnowledgeBasePort;
import com.syntracore.core.ports.AiServicePort;
import com.syntracore.core.ports.TicketRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepositoryPort ticketRepository;
    private final KnowledgeBasePort knowledgeBase;
    private final OpenAiPort openAiPort;

    // --- Bestehende Methoden ---
    public Ticket createTicket(String title, String description) {
        Ticket newTicket = new Ticket(title, description, false, LocalDateTime.now());
        return ticketRepository.save(newTicket);
    }

    public List<Ticket> getOpenTickets() {
        return ticketRepository.findAll().stream()
                .filter(ticket -> !ticket.isResolved())
                .toList();
    }

    // --- NEUE METHODEN FÜR ADMIN-BEREICH ---
    public KnowledgeEntry addKnowledge(KnowledgeEntry entry) {
        return knowledgeBase.save(entry); // knowledgeBase muss save-Methode haben!
    }

    public List<KnowledgeEntry> getAllKnowledge() {
        return knowledgeBase.findAll(); // knowledgeBase muss findAll-Methode haben!
    }

    public void resolveTicket(Long ticketId) {
        ticketRepository.findById(ticketId).ifPresent(ticket -> {
            ticket.setResolved(true);
            ticketRepository.save(ticket);
        });
    }

    // --- NEU: RAG-Antwort Methode ---
    public String getBotResponse(String userQuery) {
        List<String> relevantContexts = knowledgeBase.findRelevantContext(userQuery);
        String context = String.join("\n", relevantContexts);

        if (context.isEmpty()) {
            return "Ich konnte dazu leider nichts in unserer Wissensdatenbank finden. Möchtest du ein Ticket erstellen?";
        }

        String prompt = "Antworte auf die Frage des Benutzers basierend auf dem folgenden Kontext:\n" +
                "Kontext: " + context + "\n" +
                "Frage: " + userQuery + "\n" +
                "Antwort:";
        return openAiPort.getCompletion(prompt);
    }
}