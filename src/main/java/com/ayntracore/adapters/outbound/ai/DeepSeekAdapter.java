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
 * Konkreter Outbound-Adapter für die DeepSeek API.
 * Die API ist weitestgehend OpenAI-kompatibel.
 */
@Slf4j
@Component
public class DeepSeekAdapter implements UniversalAiPort {

    private final RestClient restClient;
    private final String model;

    public DeepSeekAdapter(@Value("${ayntracore.ai.deepseek.api-key:}") String apiKey,
                           @Value("${ayntracore.ai.deepseek.model:deepseek-chat}") String model) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.deepseek.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.model = model;
    }

    @Override
    public AiResponse generateResponse(AiChatRequest request) {
        log.info("Generating DeepSeek response for model: {}", request.model() != null ? request.model() : model);

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
                throw new RuntimeException("Empty or invalid response from DeepSeek");
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            String content = (String) message.get("content");

            Map<String, Object> usage = (Map<String, Object>) response.get("usage");
            long promptTokens = usage != null ? ((Number) usage.get("prompt_tokens")).longValue() : 0;
            long completionTokens = usage != null ? ((Number) usage.get("completion_tokens")).longValue() : 0;
            long totalTokens = usage != null ? ((Number) usage.get("total_tokens")).longValue() : 0;

            log.info("DeepSeek usage: {} prompt, {} completion, {} total tokens", promptTokens, completionTokens, totalTokens);

            AiResponseMetadata metadata = new AiResponseMetadata(
                    (String) response.get("model"),
                    promptTokens,
                    completionTokens,
                    totalTokens,
                    AiProvider.DEEPSEEK,
                    (String) firstChoice.get("finish_reason")
            );

            return new AiResponse(content, metadata);

        } catch (Exception e) {
            log.error("Error calling DeepSeek API: {}", e.getMessage());
            throw new RuntimeException("AI Provider currently unavailable (DeepSeek)", e);
        }
    }

    @Override
    public Flow.Publisher<AiResponse> generateStreamingResponse(AiChatRequest request) {
        log.warn("Streaming not fully implemented for DeepSeekAdapter. Falling back to sync.");
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

    private List<Map<String, String>> createMessages(AiChatRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        if (request.systemPrompt() != null && !request.systemPrompt().isBlank()) {
            messages.add(Map.of("role", "system", "content", request.systemPrompt()));
        }
        messages.add(Map.of("role", "user", "content", request.userMessage()));
        return messages;
    }
}
