// Autor: Christian Langner
package com.ayntracore.core.application;

import com.ayntracore.core.domain.AiChatRequest;
import com.ayntracore.core.domain.KnowledgeEntry;
import com.ayntracore.core.domain.Persona;
import com.ayntracore.core.domain.ChatMessage;
import com.ayntracore.core.ports.ChatMessageRepository;
import com.ayntracore.core.ports.UniversalAiPort;
import com.ayntracore.core.ports.VectorSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class RAGCoordinationService {

    private static final int DEFAULT_CONTEXT_LIMIT = 5;
    private static final double DEFAULT_MIN_SIMILARITY = 0.5;

    private final VectorSearchPort vectorSearchPort;
    private final UniversalAiPort aiServicePort;
    private final ContentSafetyService contentSafetyService;
    private final ChatMessageRepository chatMessageRepository;

    public String generateResponseWithContext(String userMessage, Persona persona) {
        log.debug("Generating RAG response for company: {}", persona.getCompanyId());

        if (!contentSafetyService.isSafeInput(userMessage)) {
            log.warn("Unsafe input detected for company: {}", persona.getCompanyId());
            return "I cannot process this request due to safety concerns.";
        }

        List<KnowledgeEntry> relevantKnowledge = vectorSearchPort.findSimilarContext(
                userMessage,
                persona.getCompanyId(),
                DEFAULT_CONTEXT_LIMIT
        );

        log.info("Retrieved {} knowledge entries for company: {}", relevantKnowledge.size(), persona.getCompanyId());

        List<String> contexts = relevantKnowledge.stream()
                .map(KnowledgeEntry::getContent)
                .toList();

        List<String> filteredContexts = contentSafetyService.filterExplicitContexts(contexts, persona);

        String combinedContext = String.join("\n\n---\n\n", filteredContexts);

        log.debug("Combined context length: {} characters", combinedContext.length());

        AiChatRequest aiRequest = createAiRequest(userMessage, combinedContext, persona);
        return aiServicePort.generateResponse(aiRequest).content();
    }

    public RAGResponse generateResponseWithContextAdvanced(
            String userMessage,
            Persona persona,
            int contextLimit,
            double minSimilarity
    ) {
        log.debug("Generating advanced RAG response with limit={}, minSimilarity={}", contextLimit, minSimilarity);

        if (!contentSafetyService.isSafeInput(userMessage)) {
            log.warn("Unsafe input detected for company: {}", persona.getCompanyId());
            return RAGResponse.error("Unsafe input detected");
        }

        List<VectorSearchPort.ScoredKnowledge> scoredKnowledge = vectorSearchPort.findSimilarContextWithScore(
                userMessage,
                persona.getCompanyId(),
                contextLimit,
                minSimilarity
        );

        log.info("Retrieved {} scored knowledge entries (minSimilarity: {})", scoredKnowledge.size(), minSimilarity);

        List<String> contexts = scoredKnowledge.stream()
                .map(sk -> sk.knowledgeEntry().getContent())
                .toList();

        List<String> filteredContexts = contentSafetyService.filterExplicitContexts(contexts, persona);

        String combinedContext = String.join("\n\n---\n\n", filteredContexts);

        List<ChatMessage> chatHistory = chatMessageRepository.findTop10ByCompanyIdOrderByTimestampDesc(persona.getCompanyId());
        Collections.reverse(chatHistory);
        AiChatRequest aiRequest = createAiRequest(userMessage, combinedContext, persona, chatHistory);
        String llmResponse = aiServicePort.generateResponse(aiRequest).content();

        List<ContextMetadata> metadata = scoredKnowledge.stream()
                .map(sk -> new ContextMetadata(
                        sk.knowledgeEntry().getId(),
                        sk.knowledgeEntry().getCategory(),
                        sk.knowledgeEntry().getSource(),
                        sk.similarity()
                ))
                .toList();

        return RAGResponse.success(llmResponse, metadata, filteredContexts.size());
    }

    public KnowledgeEntry addKnowledgeWithEmbedding(KnowledgeEntry knowledgeEntry) {
        log.info("Adding knowledge entry with embedding for company: {}", knowledgeEntry.getCompanyId());

        if (knowledgeEntry.getContent() == null || knowledgeEntry.getContent().isBlank()) {
            throw new IllegalArgumentException("Knowledge content cannot be empty");
        }

        return vectorSearchPort.saveWithEmbedding(knowledgeEntry);
    }

    public String generateTicketAnalysisWithContext(String ticketMessage, Persona persona) {
        log.debug("Generating ticket analysis with RAG for company: {}", persona.getCompanyId());

        List<KnowledgeEntry> relevantKnowledge = vectorSearchPort.findSimilarContext(
                ticketMessage,
                persona.getCompanyId(),
                DEFAULT_CONTEXT_LIMIT
        );

        List<String> contexts = relevantKnowledge.stream()
                .map(KnowledgeEntry::getContent)
                .toList();

        List<String> filteredContexts = contentSafetyService.filterExplicitContexts(contexts, persona);

        String combinedContext = String.join("\n\n---\n\n", filteredContexts);

        AiChatRequest aiRequest = createAiRequest("Analyze this support ticket: " + ticketMessage, combinedContext, persona);
        return aiServicePort.generateResponse(aiRequest).content();
    }

    private AiChatRequest createAiRequest(String input, String context, Persona persona, List<ChatMessage> chatHistory) {
        String systemPrompt = buildMasterSystemPrompt(context, persona);
        List<Map<String, Object>> messages = new ArrayList<>();
        for (ChatMessage msg : chatHistory) {
            messages.add(Map.of("role", msg.role(), "content", msg.content()));
        }

        return new AiChatRequest(systemPrompt, input, messages, 0.7, null, null, null);
    }

    private AiChatRequest createAiRequest(String input, String context, Persona persona) {
        String systemPrompt = buildMasterSystemPrompt(context, persona);
        return AiChatRequest.of(systemPrompt, input);
    }

    private String buildMasterSystemPrompt(String context, Persona persona) {
        if (persona == null) {
            throw new IllegalArgumentException("Persona is required but was not provided.");
        }
        if (persona.getSystemPrompt() == null || persona.getSystemPrompt().isBlank()) {
            throw new IllegalArgumentException("Persona system prompt is missing or empty.");
        }
        
        String promptContext = (context != null && !context.isEmpty()) ? context : "No specific context provided.";
        String effectiveSystemPrompt = persona.getSystemPrompt();
        String effectiveStyle = (persona.getSpeakingStyle() == null || persona.getSpeakingStyle().isBlank())
                ? "Sardonisch, trocken, desinteressiert."
                : persona.getSpeakingStyle();
        String effectiveName = (persona.getName() == null || persona.getName().isBlank())
                ? "Astra"
                : persona.getName();

        StringBuilder traitsSb = new StringBuilder();
        if (persona.getTraits() != null) {
            persona.getTraits().forEach((k, v) -> traitsSb.append("- ").append(k).append(": ").append(v).append("\n"));
        }
        String traitsBlock = traitsSb.length() > 0 ? traitsSb.toString().trim() : "- (keine)";

        String template = (persona.getPromptTemplate() == null || persona.getPromptTemplate().isBlank())
                ? "[IDENTITÄT: {{systemPrompt}}] [KONTEXT: {{context}}] [STIL: {{speakingStyle}}] User: {{input}} Astra (zynisch):"
                : persona.getPromptTemplate();

        return template
                .replace("{{systemPrompt}}", effectiveSystemPrompt)
                .replace("{{speakingStyle}}", effectiveStyle)
                .replace("{{name}}", effectiveName)
                .replace("{{traits}}", traitsBlock)
                .replace("{{context}}", promptContext);
    }

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

    public record ContextMetadata(
            UUID knowledgeId,
            String category,
            String source,
            double similarity
    ) {
    }
}
