// Autor: Christian Langner
package com.ayntracore.adapters.outbound.ai;

import com.ayntracore.core.domain.AiChatRequest;
import com.ayntracore.core.domain.AiProvider;
import com.ayntracore.core.domain.AiResponse;
import com.ayntracore.core.domain.AiResponseMetadata;
import com.ayntracore.core.ports.UniversalAiPort;
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

/**
 * Konkreter Outbound-Adapter für die OpenAI API.
 * Nutzt Spring RestClient für die Kommunikation.
 */
@Slf4j
@Component
public class OpenAiAdapter implements UniversalAiPort {

    private final RestClient restClient;
    private final String model;

    public OpenAiAdapter(@Value("${ayntracore.ai.openai.api-key:}") String apiKey,
                         @Value("${ayntracore.ai.openai.model:gpt-4o}") String model) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.model = model;
    }

    @Override
    public AiResponse generateResponse(AiChatRequest request) {
        log.info("Generating OpenAI response for model: {}", request.model() != null ? request.model() : model);

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
                throw new RuntimeException("Empty or invalid response from OpenAI");
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            String content = (String) message.get("content");

            Map<String, Object> usage = (Map<String, Object>) response.get("usage");
            long promptTokens = ((Number) usage.get("prompt_tokens")).longValue();
            long completionTokens = ((Number) usage.get("completion_tokens")).longValue();
            long totalTokens = ((Number) usage.get("total_tokens")).longValue();

            log.info("OpenAI usage: {} prompt, {} completion, {} total tokens", promptTokens, completionTokens, totalTokens);

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
            log.error("Error calling OpenAI API: {}", e.getMessage());
            throw new RuntimeException("AI Provider currently unavailable (OpenAI)", e);
        }
    }

    @Override
    public Flow.Publisher<AiResponse> generateStreamingResponse(AiChatRequest request) {
        log.warn("Streaming not fully implemented for OpenAiAdapter. Falling back to sync.");
        SubmissionPublisher<AiResponse> publisher = new SubmissionPublisher<>();
        new Thread(() -> {
            try {
                AiResponse response = generateResponse(request);
                publisher.submit(response);
            } catch (Exception e) {
                // Flow.Subscriber.onError(Throwable) wird intern aufgerufen
                publisher.closeExceptionally(e);
            } finally {
                publisher.close();
            }
        }).start();
        return publisher;
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
