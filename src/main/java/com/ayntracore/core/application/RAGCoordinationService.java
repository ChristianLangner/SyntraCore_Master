// Autor: Christian Langner
package com.ayntracore.core.application;

import com.ayntracore.core.domain.AiChatRequest;
import com.ayntracore.core.domain.Knowledge;
import com.ayntracore.core.domain.Persona;
import com.ayntracore.core.ports.UniversalAiPort;
import com.ayntracore.core.ports.VectorSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Application Service für RAG-Koordination (Retrieval-Augmented Generation).
 *
 * <p><strong>Architektur-Schicht:</strong> Application Layer (Use Cases)</p>
 * <p><strong>Hexagonale Architektur:</strong> Orchestriert VectorSearchPort und UniversalAiPort</p>
 *
 * <h2>RAG-Workflow:</h2>
 * <ol>
 *   <li><strong>Retrieval:</strong> Relevante Wissenseinträge via VectorSearchPort abrufen</li>
 *   <li><strong>Augmentation:</strong> Kontext mit Content-Safety-Filterung anreichern</li>
 *   <li><strong>Generation:</strong> LLM-Response via UniversalAiPort generieren</li>
 * </ol>
 *
 * <h2>Verantwortlichkeiten:</h2>
 * <ul>
 *   <li><strong>Kontext-Retrieval:</strong> Semantische Suche mit Vektor-Embeddings</li>
 *   <li><strong>Safety-Filterung:</strong> Content-Safety basierend auf Persona-Policy</li>
 *   <li><strong>LLM-Orchestrierung:</strong> Kombinierter Kontext an LLM übergeben</li>
 *   <li><strong>Mandantenfähigkeit:</strong> Company-spezifische Wissensbasis</li>
 * </ul>
 *
 * @author Christian Langner
 * @version 1.0
 * @see VectorSearchPort
 * @see UniversalAiPort
 * @see ContentSafetyService
 * @since 2026
 */
@Service
@Profile("home")
@RequiredArgsConstructor
@Slf4j
public class RAGCoordinationService {

    /**
     * Default-Anzahl der abzurufenden Kontext-Einträge.
     */
    private static final int DEFAULT_CONTEXT_LIMIT = 5;
    /**
     * Default minimaler Similarity-Score für Relevanz.
     */
    private static final double DEFAULT_MIN_SIMILARITY = 0.7;
    private final VectorSearchPort vectorSearchPort;
    private final UniversalAiPort aiServicePort;
    private final ContentSafetyService contentSafetyService;

    /**
     * Generiert eine LLM-Response mit RAG-Kontext.
     *
     * <p>Workflow:</p>
     * <ol>
     *   <li>Input-Validierung (Safety-Check)</li>
     *   <li>Relevanten Kontext aus Vector-DB abrufen</li>
     *   <li>Kontext nach Content-Policy filtern</li>
     *   <li>LLM-Response mit Persona und Kontext generieren</li>
     * </ol>
     *
     * @param userMessage Die User-Anfrage
     * @param persona     Die aktive Persona
     * @return Die generierte LLM-Response
     * @throws IllegalArgumentException bei unsicheren Inputs
     */
    public String generateResponseWithContext(String userMessage, Persona persona) {
        log.debug("Generating RAG response for company: {}", persona.getCompanyId());

        // 1. Input-Validierung
        if (!contentSafetyService.isSafeInput(userMessage)) {
            log.warn("Unsafe input detected for company: {}", persona.getCompanyId());
            return "I cannot process this request due to safety concerns.";
        }

        // 2. Relevanten Kontext abrufen
        List<Knowledge> relevantKnowledge = vectorSearchPort.findSimilarContext(
                userMessage,
                persona.getCompanyId(),
                DEFAULT_CONTEXT_LIMIT
        );

        log.info("Retrieved {} knowledge entries for company: {}", relevantKnowledge.size(), persona.getCompanyId());

        // 3. Kontext extrahieren und filtern
        List<String> contexts = relevantKnowledge.stream()
                .map(Knowledge::getContent)
                .toList();

        List<String> filteredContexts = contentSafetyService.filterExplicitContexts(contexts, persona);

        String combinedContext = String.join("\n\n---\n\n", filteredContexts);

        log.debug("Combined context length: {} characters", combinedContext.length());

        // 4. LLM-Response generieren
        AiChatRequest aiRequest = createAiRequest(userMessage, combinedContext, persona);
        return aiServicePort.generateResponse(aiRequest).content();
    }

    /**
     * Generiert eine LLM-Response mit RAG-Kontext und konfigurierbaren Parametern.
     *
     * @param userMessage   Die User-Anfrage
     * @param persona       Die aktive Persona
     * @param contextLimit  Max. Anzahl der Kontext-Einträge
     * @param minSimilarity Minimaler Similarity-Score (0.0 - 1.0)
     * @return Die generierte LLM-Response mit Metadaten
     */
    public RAGResponse generateResponseWithContextAdvanced(
            String userMessage,
            Persona persona,
            int contextLimit,
            double minSimilarity
    ) {
        log.debug("Generating advanced RAG response with limit={}, minSimilarity={}", contextLimit, minSimilarity);

        // 1. Input-Validierung
        if (!contentSafetyService.isSafeInput(userMessage)) {
            log.warn("Unsafe input detected for company: {}", persona.getCompanyId());
            return RAGResponse.error("Unsafe input detected");
        }

        // 2. Relevanten Kontext mit Score abrufen
        List<VectorSearchPort.ScoredKnowledge> scoredKnowledge = vectorSearchPort.findSimilarContextWithScore(
                userMessage,
                persona.getCompanyId(),
                contextLimit,
                minSimilarity
        );

        log.info("Retrieved {} scored knowledge entries (minSimilarity: {})", scoredKnowledge.size(), minSimilarity);

        // 3. Kontext extrahieren und filtern
        List<String> contexts = scoredKnowledge.stream()
                .map(sk -> sk.knowledge().getContent())
                .toList();

        List<String> filteredContexts = contentSafetyService.filterExplicitContexts(contexts, persona);

        String combinedContext = String.join("\n\n---\n\n", filteredContexts);

        // 4. LLM-Response generieren
        AiChatRequest aiRequest = createAiRequest(userMessage, combinedContext, persona);
        String llmResponse = aiServicePort.generateResponse(aiRequest).content();

        // 5. Metadaten sammeln
        List<ContextMetadata> metadata = scoredKnowledge.stream()
                .map(sk -> new ContextMetadata(
                        sk.knowledge().getId(),
                        sk.knowledge().getCategory(),
                        sk.knowledge().getSource(),
                        sk.similarity()
                ))
                .toList();

        return RAGResponse.success(llmResponse, metadata, filteredContexts.size());
    }

    /**
     * Speichert einen neuen Knowledge-Eintrag mit automatischer Embedding-Generierung.
     *
     * @param knowledge Der zu speichernde Knowledge-Eintrag
     * @return Der gespeicherte Knowledge-Eintrag mit Embedding
     */
    public Knowledge addKnowledgeWithEmbedding(Knowledge knowledge) {
        log.info("Adding knowledge entry with embedding for company: {}", knowledge.getCompanyId());

        if (knowledge.getContent() == null || knowledge.getContent().isBlank()) {
            throw new IllegalArgumentException("Knowledge content cannot be empty");
        }

        return vectorSearchPort.saveWithEmbedding(knowledge);
    }

    /**
     * Generiert eine Ticket-Analyse mit RAG-Kontext.
     *
     * @param ticketMessage Der Ticket-Inhalt
     * @param persona       Die aktive Persona
     * @return Die generierte Analyse
     */
    public String generateTicketAnalysisWithContext(String ticketMessage, Persona persona) {
        log.debug("Generating ticket analysis with RAG for company: {}", persona.getCompanyId());

        // Relevanten Kontext abrufen
        List<Knowledge> relevantKnowledge = vectorSearchPort.findSimilarContext(
                ticketMessage,
                persona.getCompanyId(),
                DEFAULT_CONTEXT_LIMIT
        );

        // Kontext extrahieren und filtern
        List<String> contexts = relevantKnowledge.stream()
                .map(Knowledge::getContent)
                .toList();

        List<String> filteredContexts = contentSafetyService.filterExplicitContexts(contexts, persona);

        String combinedContext = String.join("\n\n---\n\n", filteredContexts);

        // Ticket-Analyse via UniversalAiPort generieren
        // Note: SupportTicket benötigt für generateAnalysis - hier vereinfachte Version
        AiChatRequest aiRequest = createAiRequest("Analyze this support ticket: " + ticketMessage, combinedContext, persona);
        return aiServicePort.generateResponse(aiRequest).content();
    }

    private AiChatRequest createAiRequest(String input, String context, Persona persona) {
        String systemPrompt = buildMasterSystemPrompt(context, persona);
        return AiChatRequest.of(systemPrompt, input);
    }

    private String buildMasterSystemPrompt(String context, Persona persona) {
        String promptContext = (context != null && !context.isEmpty()) ? context : "Allgemeiner Support.";
        String effectiveSystemPrompt = (persona == null || persona.getSystemPrompt() == null || persona.getSystemPrompt().isBlank())
                ? "Du bist ein Support-Experte."
                : persona.getSystemPrompt();
        String effectiveStyle = (persona == null || persona.getSpeakingStyle() == null || persona.getSpeakingStyle().isBlank())
                ? "Freundlich, klar, präzise."
                : persona.getSpeakingStyle();
        String effectiveName = (persona == null || persona.getName() == null || persona.getName().isBlank())
                ? "Support Assistant"
                : persona.getName();

        StringBuilder traitsSb = new StringBuilder();
        if (persona != null && persona.getTraits() != null) {
            persona.getTraits().forEach((k, v) -> traitsSb.append("- ").append(k).append(": ").append(v).append("\n"));
        }
        String traitsBlock = traitsSb.length() > 0 ? traitsSb.toString().trim() : "- (keine)";

        String template = (persona == null || persona.getPromptTemplate() == null || persona.getPromptTemplate().isBlank())
                ? Persona.defaultTemplate()
                : persona.getPromptTemplate();

        return template
                .replace("{{systemPrompt}}", effectiveSystemPrompt)
                .replace("{{speakingStyle}}", effectiveStyle)
                .replace("{{name}}", effectiveName)
                .replace("{{traits}}", traitsBlock)
                .replace("{{context}}", promptContext);
    }

    /**
     * RAG-Response mit Metadaten.
     */
    public record RAGResponse(
            String llmResponse,
            List<ContextMetadata> usedContexts,
            int contextCount,
            boolean success,
            String errorMessage
    ) {
        public static RAGResponse success(String llmResponse, List<ContextMetadata> usedContexts, int contextCount) {
            return new RAGResponse(llmResponse, usedContexts, contextCount, true, null);
        }

        public static RAGResponse error(String errorMessage) {
            return new RAGResponse(null, List.of(), 0, false, errorMessage);
        }
    }

    /**
     * Metadaten für verwendeten Kontext.
     */
    public record ContextMetadata(
            UUID knowledgeId,
            String category,
            String source,
            double similarity
    ) {
    }
}
