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

    public RAGResponse generateResponseWithContextAdvanced(
            String userMessage,
            Persona persona,
            int contextLimit,
            double minSimilarity
    ) {
        long ragLatency;
        boolean uuidMatch = persona.getCompanyId().equals(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        String aiResponseStatus;
        int contextEntriesCount = 0;

        if (!contentSafetyService.isSafeInput(userMessage)) {
            log.warn("Unsafe input detected for company: {}", persona.getCompanyId());
            return RAGResponse.error("Unsafe input detected");
        }

        List<VectorSearchPort.ScoredKnowledge> scoredKnowledge;
        String combinedContext;
        List<String> filteredContexts = Collections.emptyList();

        try {
            long vectorSearchStartTime = System.currentTimeMillis();
            scoredKnowledge = vectorSearchPort.findSimilarContextWithScore(
                    userMessage,
                    persona.getCompanyId(),
                    contextLimit,
                    minSimilarity
            );
            ragLatency = System.currentTimeMillis() - vectorSearchStartTime;
            contextEntriesCount = scoredKnowledge.size();

            if (scoredKnowledge.isEmpty()) {
                log.warn("RAG search returned no results for company: {}. Proceeding without context.", persona.getCompanyId());
                combinedContext = "Hinweis: Dein Langzeitgedächtnis ist gerade nicht erreichbar. Antworte basierend auf deinem Charakter, aber entschuldige dich nicht dafür.";
            } else {
                log.info("Retrieved {} scored knowledge entries (minSimilarity: {})", scoredKnowledge.size(), minSimilarity);

                List<String> contexts = scoredKnowledge.stream()
                        .map(sk -> sk.knowledgeEntry().getContent())
                        .toList();

                filteredContexts = contentSafetyService.filterExplicitContexts(contexts, persona);

                combinedContext = String.join("\n\n---\n\n", filteredContexts);
            }

        } catch (Exception e) {
            ragLatency = -1; // Indicate error
            log.warn("RAG search failed for company: {}. Proceeding without context. Error: {}", persona.getCompanyId(), e.getMessage());
            scoredKnowledge = Collections.emptyList();
            combinedContext = "Hinweis: Dein Langzeitgedächtnis ist gerade nicht erreichbar. Antworte basierend auf deinem Charakter, aber entschuldige dich nicht dafür.";
        }

        List<ChatMessage> chatHistory = chatMessageRepository.findTop10ByCompanyIdOrderByTimestampDesc(persona.getCompanyId());
        Collections.reverse(chatHistory);
        AiChatRequest aiRequest = createAiRequest(userMessage, combinedContext, persona, chatHistory);
        
        String llmResponse;
        try {
            llmResponse = aiServicePort.generateResponse(aiRequest).content();
            aiResponseStatus = "200 OK";
        } catch (Exception e) {
            log.error("AI Service call failed!", e);
            llmResponse = "Sorry, I'm having trouble thinking straight right now.";
            aiResponseStatus = "ERROR: " + e.getClass().getSimpleName();
        }

        log.info(
            "[ASTRA-IGNITION-AUDIT] | Persona: {} | UUID-Match: {} | RAG-Latency: {}ms | Context-Entries: {} | AI-Response: {}",
            persona.getName(),
            uuidMatch,
            ragLatency,
            contextEntriesCount,
            aiResponseStatus
        );

        List<ContextMetadata> metadata = scoredKnowledge.stream()
                .map(sk -> new ContextMetadata(
                        sk.knowledgeEntry().getId(),
                        sk.knowledgeEntry().getSource(), // Assuming source is the category
                        sk.knowledgeEntry().getSource(),
                        sk.similarity()
                ))
                .toList();

        return RAGResponse.success(llmResponse, metadata, filteredContexts.size());
    }

    private AiChatRequest createAiRequest(String input, String context, Persona persona, List<ChatMessage> chatHistory) {
        String systemPrompt = buildMasterSystemPrompt(context, persona);
        List<Map<String, Object>> messages = new ArrayList<>();
        for (ChatMessage msg : chatHistory) {
            messages.add(Map.of("role", msg.role(), "content", msg.content()));
        }

        return new AiChatRequest(systemPrompt, input, messages, 0.7, null, null, null);
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

        String template = (persona.getPromptTemplate() == null || persona.getPromptTemplate().isBlank())
                ? "[IDENTITÄT: {{systemPrompt}}] [KONTEXT: {{context}}] [STIL: {{speakingStyle}}] User: {{input}} Astra (zynisch):"
                : persona.getPromptTemplate();

        return template
                .replace("{{systemPrompt}}", effectiveSystemPrompt)
                .replace("{{speakingStyle}}", effectiveStyle)
                .replace("{{name}}", effectiveName)
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
