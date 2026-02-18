// Autor: Christian Langner
package com.ayntracore.core.services;

import com.ayntracore.core.domain.AiChatRequest;
import com.ayntracore.core.domain.KnowledgeEntry;
import com.ayntracore.core.domain.Persona;
import com.ayntracore.core.domain.SupportTicket;
import com.ayntracore.core.ports.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Orchestrates all business logic related to support tickets, inquiries, and persona management.
 * This service acts as the central use case implementation for the ticket system.
 *
 * @implements TicketUseCase Port interface for ticket and inquiry processing.
 */
@Service
@RequiredArgsConstructor
public class TicketService implements TicketUseCase {

    private static final String DEFAULT_SYSTEM_PROMPT = "Du bist ein hilfreicher Support-Assistent.";
    private static final String DEFAULT_SPEAKING_STYLE = "Freundlich, klar, präzise.";

    // --- Injected Ports ---
    private final TicketRepositoryPort ticketRepository;
    private final KnowledgeBasePort knowledgeBase;
    private final UniversalAiPort aiService;
    private final PersonaRepositoryPort personaRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    /**
     * Creates or updates the active persona for a given company.
     * Parses a JSON string for traits and ensures default templates are set.
     *
     * @param companyId     The ID of the company.
     * @param name          The new name for the persona.
     * @param prompt        The new system prompt.
     * @param style         The new speaking style.
     * @param traitsJson    A JSON string representing persona traits.
     * @param promptTemplate The custom prompt template.
     * @param exampleDialog  An example conversation to guide the AI.
     */
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
            Map<String, Object> raw = objectMapper.readValue(traitsJson, new TypeReference<Map<String, Object>>() {
            });
            Map<String, String> result = new LinkedHashMap<>();
            raw.forEach((k, v) -> result.put(k, v == null ? "" : String.valueOf(v)));
            return result;
        } catch (Exception e) {
            Map<String, String> fallback = new LinkedHashMap<>();
            fallback.put("traitsJsonError", "Invalid JSON: " + e.getMessage());
            return fallback;
        }
    }

    /**
     * Retrieves the active persona for a company, applying default values if none exists.
     * This ensures that the system always has a valid persona to work with.
     *
     * @param companyId The ID of the company.
     * @return The active or a default Persona.
     */
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

    /**
     * Creates a new support ticket, enriches it with context from the knowledge base,
     * and performs an initial AI analysis.
     *
     * Data Flow:
     * 1. A new SupportTicket is created.
     * 2. The user's message is used to find relevant entries in the knowledge base (RAG).
     * 3. The persona for the company is loaded.
     * 4. A "master" system prompt is constructed from the persona and the RAG context.
     * 5. The AI service is called to analyze the ticket based on the master prompt.
     * 6. The analysis is saved with the ticket in the database.
     *
     * @param customerName The name of the customer creating the ticket.
     * @param message      The customer's support request.
     * @param customerId   The ID of the customer/company.
     */
    @Override
    public void createAndProcessTicket(String customerName, String message, UUID customerId) {
        UUID companyId = customerId; // Assume customerId is the companyId for multi-tenancy
        SupportTicket ticket = new SupportTicket(customerName, message, companyId);

        // 1. RAG: Fetch relevant context from the knowledge base
        List<KnowledgeEntry> contextEntries = knowledgeBase.findRelevantEntries(message, companyId, null);
        String combinedContext = contextEntries.stream()
                .map(KnowledgeEntry::getContent)
                .collect(Collectors.joining("\n"));

        // 2. Load the persona that defines the AI's behavior and traits
        Persona persona = getPersonaForCompany(companyId);

        // 3. Build a comprehensive system prompt for the AI
        String systemPrompt = buildMasterSystemPrompt(combinedContext, persona);

        // 4. Call the AI to get an initial analysis of the support ticket
        String analysis = aiService.generateResponse(AiChatRequest.of(systemPrompt, "Analyze this ticket: " + message)).content();
        ticket.setAiAnalysis(analysis);

        // 5. Persist the newly created and analyzed ticket
        ticketRepository.save(ticket);
    }

    /**
     * Processes a direct user inquiry by leveraging the RAG system and the active persona.
     *
     * Data Flow:
     * 1. The active persona for the company is loaded.
     * 2. A potential 'appCategory' trait from the persona is used to filter the RAG search.
     * 3. Relevant knowledge base entries are fetched based on the user message and category.
     * 4. A master system prompt is built using the persona and the fetched context.
     * 5. The AI service is called to generate a direct response to the user's message.
     *
     * @param userMessage The direct question from the user.
     * @param customerId  The ID of the customer/company.
     * @return A string containing the AI-generated response.
     */
    @Override
    public String processInquiry(String userMessage, UUID customerId) {
        UUID companyId = customerId;
        Persona persona = getPersonaForCompany(companyId);

        // RAG Enhancement: Use a persona trait to filter the knowledge base search.
        // This allows for context-aware retrieval based on the persona's configuration.
        String appCategory = persona.getTraits() != null ? persona.getTraits().get("appCategory") : null;

        List<KnowledgeEntry> contextEntries = knowledgeBase.findRelevantEntries(userMessage, companyId, appCategory);
        List<String> context = contextEntries.stream()
                .map(KnowledgeEntry::getContent)
                .collect(Collectors.toList());

        // Build the final prompt that guides the AI's response style, knowledge, and persona.
        String systemPrompt = buildMasterSystemPrompt(String.join("\n", context), persona);

        // Generate and return the final response from the AI.
        return aiService.generateResponse(AiChatRequest.of(systemPrompt, userMessage)).content();
    }


    /**
     * Constructs the master system prompt by merging a persona's configuration with dynamic context.
     * This method is central to tailoring the AI's behavior for each request.
     * It uses a template which is filled with persona traits, speaking style, and the RAG context.
     *
     * @param context The dynamically fetched RAG context from the knowledge base.
     * @param persona The active persona for the current request.
     * @return A fully constructed system prompt ready to be sent to the AI.
     */
    private String buildMasterSystemPrompt(String context, Persona persona) {
        // Use provided context or a default fallback if no specific context was found.
        String promptContext = (context != null && !context.isEmpty()) ? context : "Allgemeiner Support.";

        // Apply persona-specific system prompts and styles, with defaults for safety.
        String effectiveSystemPrompt = (persona == null || persona.getSystemPrompt() == null || persona.getSystemPrompt().isBlank())
                ? DEFAULT_SYSTEM_PROMPT
                : persona.getSystemPrompt();
        String effectiveStyle = (persona == null || persona.getSpeakingStyle() == null || persona.getSpeakingStyle().isBlank())
                ? DEFAULT_SPEAKING_STYLE
                : persona.getSpeakingStyle();
        String effectiveName = (persona == null || persona.getName() == null || persona.getName().isBlank())
                ? "Support Assistant"
                : persona.getName();

        // Format the persona's key-value traits into a readable block for the AI.
        StringBuilder traitsSb = new StringBuilder();
        if (persona != null && persona.getTraits() != null) {
            persona.getTraits().forEach((k, v) -> traitsSb.append("- ").append(k).append(": ").append(v).append("\n"));
        }
        String traitsBlock = traitsSb.length() > 0 ? traitsSb.toString().trim() : "- (keine)";

        // Use the persona's prompt template or the default one.
        String template = (persona == null || persona.getPromptTemplate() == null || persona.getPromptTemplate().isBlank())
                ? Persona.defaultTemplate()
                : persona.getPromptTemplate();

        // Inject all parts into the template to create the final, master prompt.
        return template
                .replace("{{systemPrompt}}", effectiveSystemPrompt)
                .replace("{{speakingStyle}}", effectiveStyle)
                .replace("{{name}}", effectiveName)
                .replace("{{traits}}", traitsBlock)
                .replace("{{context}}", promptContext);
    }
}
