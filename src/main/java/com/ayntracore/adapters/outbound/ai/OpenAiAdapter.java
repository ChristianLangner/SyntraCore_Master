// Autor: Christian Langner
package com.ayntracore.adapters.outbound.ai;

import com.ayntracore.core.domain.AiChatRequest;
import com.ayntracore.core.domain.AiProvider;
import com.ayntracore.core.domain.AiResponse;
import com.ayntracore.core.domain.AiResponseMetadata;
import com.ayntracore.core.ports.EmbeddingPort;
import com.ayntracore.core.ports.UniversalAiPort;
import com.pgvector.PGvector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

@Slf4j
@Component
public class OpenAiAdapter implements UniversalAiPort, EmbeddingPort {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final String embeddingModel;

    public OpenAiAdapter(@Value("${openrouter.api.key}") String apiKey,
                         @Value("${openrouter.api.url}") String baseUrl,
                         @Value("${ayntracore.ai.openai.model:openai/gpt-3.5-turbo}") String model,
                         @Value("${ayntracore.ai.openai.embedding-model:text-embedding-ada-002}") String embeddingModel) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("HTTP-Referer", "http://localhost:8080")
                .build();
        this.model = model;
        this.embeddingModel = embeddingModel;
        log.info("OpenRouter API connected");
    }

    @Override
    public AiResponse generateResponse(AiChatRequest request) {
        log.info("Generating OpenRouter response for model: {}", request.model() != null ? request.model() : model);

        try {
            Map<String, Object> body = Map.of(
                    "model", request.model() != null ? request.model() : model,
                    "messages", createMessages(request),
                    "temperature", request.temperature()
            );

            Map<String, Object> response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("choices")) {
                throw new RuntimeException("Empty or invalid response from OpenRouter");
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            String content = (String) message.get("content");

            Map<String, Object> usage = (Map<String, Object>) response.get("usage");
            long promptTokens = ((Number) usage.get("prompt_tokens")).longValue();
            long completionTokens = ((Number) usage.get("completion_tokens")).longValue();
            long totalTokens = ((Number) usage.get("total_tokens")).longValue();

            log.info("OpenRouter usage: {} prompt, {} completion, {} total tokens", promptTokens, completionTokens, totalTokens);

            AiResponseMetadata metadata = new AiResponseMetadata(
                    (String) response.get("model"),
                    promptTokens,
                    completionTokens,
                    totalTokens,
                    AiProvider.OPENAI,
                    (String) firstChoice.get("finish_reason")
            );

            return new AiResponse(content, metadata);

        } catch (Exception e) {
            log.error("Error calling OpenRouter API: {}", e.getMessage());
            throw new RuntimeException("AI Provider currently unavailable (OpenRouter)", e);
        }
    }

    @Override
    public Flow.Publisher<AiResponse> generateStreamingResponse(AiChatRequest request) {
        log.warn("Streaming not fully implemented for OpenRouter. Falling back to sync.");
        SubmissionPublisher<AiResponse> publisher = new SubmissionPublisher<>();
        new Thread(() -> {
            try {
                AiResponse response = generateResponse(request);
                publisher.submit(response);
            } catch (Exception e) {
                publisher.closeExceptionally(e);
            } finally {
                publisher.close();
            }
        }).start();
        return publisher;
    }

    @Override
    public PGvector createEmbedding(String text) {
        log.info("Creating OpenRouter embedding for model: {}", embeddingModel);
        
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[CRITICAL] API key is not configured. Returning empty vector. This will cause downstream errors.");
            return new PGvector(new float[0]);
        }

        try {
            Map<String, Object> body = Map.of(
                    "input", text,
                    "model", embeddingModel
            );

            Map<String, Object> response = restClient.post()
                    .uri("/embeddings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("data")) {
                throw new RuntimeException("Empty or invalid response from OpenRouter embeddings");
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            Map<String, Object> firstData = data.get(0);
            List<Double> embedding = (List<Double>) firstData.get("embedding");

            float[] floatArray = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                floatArray[i] = embedding.get(i).floatValue();
            }

            return new PGvector(floatArray);

        } catch (Exception e) {
            log.error("Error creating OpenRouter embedding: {}", e.getMessage());
            throw new RuntimeException("AI Provider currently unavailable for embeddings (OpenRouter)", e);
        }
    }

    private List<Map<String, String>> createMessages(AiChatRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        if (request.systemPrompt() != null && !request.systemPrompt().isBlank()) {
            messages.add(Map.of("role", "system", "content", request.systemPrompt()));
        }
        messages.add(Map.of("role", "user", "content", request.userMessage()));
        return messages;
    }
}
