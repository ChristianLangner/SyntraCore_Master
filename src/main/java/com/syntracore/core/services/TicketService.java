package com.syntracore.core.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syntracore.core.domain.KnowledgeEntry;
import com.syntracore.core.domain.Persona;
import com.syntracore.core.domain.SupportTicket;
import com.syntracore.core.ports.KnowledgeBasePort;
import com.syntracore.core.ports.AiServicePort;
import com.syntracore.core.ports.PersonaRepositoryPort;
import com.syntracore.core.ports.TicketRepositoryPort;
import com.syntracore.core.ports.TicketUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// ... existing code ...

/**
 * // UPDATE #58
 * Persona now supports flexible JSON traits + prompt template + example dialog.
 */
@Service
@RequiredArgsConstructor
public class TicketService implements TicketUseCase {

    private final TicketRepositoryPort ticketRepository;
    private final KnowledgeBasePort knowledgeBase;
    private final AiServicePort aiService;
    private final PersonaRepositoryPort personaRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DEFAULT_SYSTEM_PROMPT = "Du bist ein hilfreicher Support-Assistent.";
    private static final String DEFAULT_SPEAKING_STYLE = "Freundlich, klar, präzise.";

    /**
     * Admin: alle Knowledge-Einträge anzeigen.
     */
    public List<KnowledgeEntry> getAllKnowledge() {
        return knowledgeBase.findAll();
    }

    /**
     * Admin: Knowledge-Eintrag hinzufügen.
     */
    public KnowledgeEntry addKnowledge(KnowledgeEntry entry) {
        return knowledgeBase.save(entry);
    }

    /**
     * Admin: offene Tickets anzeigen (resolved=false).
     */
    public List<SupportTicket> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .filter(t -> !t.isResolved())
                .toList();
    }

    /**
     * Admin/WebSocket: Ticket als erledigt markieren.
     */
    public void resolveTicket(UUID ticketId) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        if (!ticket.isResolved()) {
            ticket.setResolved(true);
            ticketRepository.save(ticket);
        }
    }

    public void updatePersona(UUID companyId,
                              String name,
                              String prompt,
                              String style,
                              String traitsJson,
                              String promptTemplate,
                              String exampleDialog) {

        Persona persona = personaRepository.findActiveByCompanyId(companyId)
                .orElseGet(() -> new Persona(companyId, "Default Persona", DEFAULT_SYSTEM_PROMPT, DEFAULT_SPEAKING_STYLE));

        if (name != null && !name.isBlank()) {
            persona.setName(name);
        }
        persona.setSystemPrompt(prompt);
        persona.setSpeakingStyle(style);

        // traits JSON -> Map<String, String>
        persona.setTraits(parseTraitsJson(traitsJson));

        if (promptTemplate != null && !promptTemplate.isBlank()) {
            persona.setPromptTemplate(promptTemplate);
        } else if (persona.getPromptTemplate() == null || persona.getPromptTemplate().isBlank()) {
            persona.setPromptTemplate(Persona.defaultTemplate());
        }

        if (exampleDialog != null && !exampleDialog.isBlank()) {
            persona.setExampleDialog(exampleDialog);
        } else {
            persona.setExampleDialog(null);
        }

        personaRepository.save(persona);
    }

    public Persona getCurrentPersona(UUID companyId) {
        return getPersonaForCompany(companyId);
    }

    private Map<String, String> parseTraitsJson(String traitsJson) {
        if (traitsJson == null || traitsJson.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            Map<String, Object> raw = objectMapper.readValue(traitsJson, new TypeReference<Map<String, Object>>() {});
            Map<String, String> result = new LinkedHashMap<>();
            raw.forEach((k, v) -> result.put(k, v == null ? "" : String.valueOf(v)));
            return result;
        } catch (Exception e) {
            Map<String, String> fallback = new LinkedHashMap<>();
            fallback.put("traitsJsonError", "Invalid JSON: " + e.getMessage());
            return fallback;
        }
    }

    private Persona getPersonaForCompany(UUID companyId) {
        Persona persona = personaRepository.findActiveByCompanyId(companyId)
                .orElseGet(() -> new Persona(companyId, "Default Persona", DEFAULT_SYSTEM_PROMPT, DEFAULT_SPEAKING_STYLE));

        if (persona.getTraits() == null) {
            persona.setTraits(new LinkedHashMap<>());
        }
        if (persona.getPromptTemplate() == null || persona.getPromptTemplate().isBlank()) {
            persona.setPromptTemplate(Persona.defaultTemplate());
        }
        return persona;
    }

    @Override
    public void createAndProcessTicket(String customerName, String message, UUID customerId) {
        UUID companyId = customerId;
        SupportTicket ticket = new SupportTicket(customerName, message, companyId);

        List<String> context = knowledgeBase.findRelevantContext(message, companyId);
        String combinedContext = String.join("\n", context);

        Persona persona = getPersonaForCompany(companyId);

        String analysis = aiService.generateAnalysis(ticket, combinedContext, persona);
        ticket.setAiAnalysis(analysis);

        ticketRepository.save(ticket);
    }

    @Override
    public String processInquiry(String userMessage, UUID customerId) {
        UUID companyId = customerId;
        List<String> context = knowledgeBase.findRelevantContext(userMessage, companyId);

        Persona persona = getPersonaForCompany(companyId);

        return aiService.generateChatResponse(userMessage, String.join("\n", context), persona);
    }

    // ... existing code ...
}