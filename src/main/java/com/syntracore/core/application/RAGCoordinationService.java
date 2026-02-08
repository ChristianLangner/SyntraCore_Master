// Autor: Christian Langner
package com.syntracore.core.application;

import com.syntracore.core.domain.Knowledge;
import com.syntracore.core.domain.Persona;
import com.syntracore.core.ports.AiServicePort;
import com.syntracore.core.ports.VectorSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application Service für RAG-Koordination (Retrieval-Augmented Generation).
 *
 * <p><strong>Architektur-Schicht:</strong> Application Layer (Use Cases)</p>
 * <p><strong>Hexagonale Architektur:</strong> Orchestriert VectorSearchPort und AiServicePort</p>
 *
 * <h2>RAG-Workflow:</h2>
 * <ol>
 *   <li><strong>Retrieval:</strong> Relevante Wissenseinträge via VectorSearchPort abrufen</li>
 *   <li><strong>Augmentation:</strong> Kontext mit Content-Safety-Filterung anreichern</li>
 *   <li><strong>Generation:</strong> LLM-Response via AiServicePort generieren</li>
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
 * @since 2026
 *
 * @see VectorSearchPort
 * @see AiServicePort
 * @see ContentSafetyService
 */
@Service
@Profile("home")
@RequiredArgsConstructor
@Slf4j
public class RAGCoordinationService {

    private final VectorSearchPort vectorSearchPort;
    private final AiServicePort aiServicePort;
    private final ContentSafetyService contentSafetyService;

    /**
     * Default-Anzahl der abzurufenden Kontext-Einträge.
     */
    private static final int DEFAULT_CONTEXT_LIMIT = 5;

    /**
     * Default minimaler Similarity-Score für Relevanz.
     */
    private static final double DEFAULT_MIN_SIMILARITY = 0.7;

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
     * @param persona Die aktive Persona
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
        return aiServicePort.generateChatResponse(userMessage, combinedContext, persona);
    }

    /**
     * Generiert eine LLM-Response mit RAG-Kontext und konfigurierbaren Parametern.
     *
     * @param userMessage Die User-Anfrage
     * @param persona Die aktive Persona
     * @param contextLimit Max. Anzahl der Kontext-Einträge
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
        String llmResponse = aiServicePort.generateChatResponse(userMessage, combinedContext, persona);

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
     * @param persona Die aktive Persona
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

        // Ticket-Analyse via AiServicePort generieren
        // Note: SupportTicket benötigt für generateAnalysis - hier vereinfachte Version
        return aiServicePort.generateChatResponse(
                "Analyze this support ticket: " + ticketMessage,
                combinedContext,
                persona
        );
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
