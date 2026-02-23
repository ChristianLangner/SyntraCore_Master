// Autor: Christian Langner
package com.ayntracore.adapters.outbound.ai;

import com.ayntracore.core.domain.AiChatRequest;
import com.ayntracore.core.domain.AiProvider;
import com.ayntracore.core.domain.AiResponse;
import com.ayntracore.core.domain.AiResponseMetadata;
import com.ayntracore.core.ports.EmbeddingPort;
import com.ayntracore.core.ports.ImageGenerationPort;
import com.ayntracore.core.ports.UniversalAiPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgvector.PGvector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

@Slf4j
@Component
public class OpenAiAdapter implements UniversalAiPort, EmbeddingPort, ImageGenerationPort {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final String embeddingModel;
    private final String imageModel;
    private final String cheapImageModel;
    private final String unfilteredImageModel;
    private final ObjectMapper objectMapper;

    public OpenAiAdapter(@Value("${ai.openrouter.key}") String apiKey,
                         @Value("${openrouter.api.url}") String baseUrl,
                         @Value("${ai.model.chat:gpt-4o}") String model,
                         @Value("${ai.model.embedding:text-embedding-3-small}") String embeddingModel,
                         @Value("${ai.model.image}") String imageModel,
                         @Value("${ai.model.image.cheap}") String cheapImageModel,
                         @Value("${ai.model.image.unfiltered}") String unfilteredImageModel,
                         ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("HTTP-Referer", "https://ayntra.ai")
                .defaultHeader("X-Title", "AyntraCore")
                .build();
        this.model = model;
        this.embeddingModel = embeddingModel;
        this.imageModel = imageModel;
        this.cheapImageModel = cheapImageModel;
        this.unfilteredImageModel = unfilteredImageModel;
        this.objectMapper = objectMapper;
        log.info("OpenRouter API connected");
    }

    @Override
    public AiResponse generateResponse(AiChatRequest request) {
        log.info("Generating OpenRouter response for model: {}", request.model() != null ? request.model() : model);

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", request.model() != null ? request.model() : model,
                    "messages", createMessages(request),
                    "temperature", request.temperature()
            );

            log.debug("Request Payload: {}", objectMapper.writeValueAsString(requestBody));

            Map<String, Object> response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("choices")) {
                log.error("Invalid response from OpenRouter: No 'choices' field");
                return AiResponse.error("AI provider returned an invalid response.");
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices.isEmpty()) {
                log.error("Invalid response from OpenRouter: 'choices' array is empty");
                return AiResponse.error("AI provider returned no choices.");
            }

            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            String content = (String) message.get("content");

            Map<String, Object> usage = (Map<String, Object>) response.getOrDefault("usage", Map.of());
            long promptTokens = ((Number) usage.getOrDefault("prompt_tokens", 0)).longValue();
            long completionTokens = ((Number) usage.getOrDefault("completion_tokens", 0)).longValue();
            long totalTokens = ((Number) usage.getOrDefault("total_tokens", 0)).longValue();

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

        } catch (HttpClientErrorException e) {
            log.error("HTTP Error calling OpenRouter API: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return AiResponse.error("AI Provider is currently experiencing issues (HTTP Error).");
        } catch (Exception e) {
            log.error("Error calling OpenRouter API: {}", e.getMessage(), e);
            return AiResponse.error("An unexpected error occurred with the AI Provider.");
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
        log.info("Creating embedding for text using model: {}", embeddingModel);
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", embeddingModel,
                    "input", text
            );

            Map<String, Object> response = restClient.post()
                    .uri("/embeddings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("data")) {
                log.error("Invalid response from OpenRouter embeddings: No 'data' field");
                return new PGvector(new float[0]);
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            if (data.isEmpty() || !data.get(0).containsKey("embedding")) {
                log.error("Invalid response from OpenRouter embeddings: 'data' is empty or missing 'embedding'");
                return new PGvector(new float[0]);
            }

            List<Double> embeddingDouble = (List<Double>) data.get(0).get("embedding");
            float[] embeddingFloat = new float[embeddingDouble.size()];
            for (int i = 0; i < embeddingDouble.size(); i++) {
                embeddingFloat[i] = embeddingDouble.get(i).floatValue();
            }

            return new PGvector(embeddingFloat);

        } catch (HttpClientErrorException e) {
            log.error("HTTP Error calling OpenRouter embeddings API: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return new PGvector(new float[0]);
        } catch (Exception e) {
            log.error("Error calling OpenRouter embeddings API: {}", e.getMessage(), e);
            return new PGvector(new float[0]);
        }
    }

    private List<Map<String, Object>> createMessages(AiChatRequest request) {
        List<Map<String, Object>> messages = new ArrayList<>();

        // Use the system prompt from the request.
        if (request.systemPrompt() != null && !request.systemPrompt().isBlank()) {
            messages.add(Map.of("role", "system", "content", request.systemPrompt()));
        }

        // Add historical messages if they exist.
        if (request.messages() != null && !request.messages().isEmpty()) {
            messages.addAll(request.messages());
        }

        // Finally, add the current user's message.
        messages.add(Map.of("role", "user", "content", request.userMessage()));

        return messages;
    }

    @Override
    public ImageGenerationResponse generateImage(ImageGenerationRequest request) {
        log.info("Generating OpenRouter image for model: {}", imageModel);

        String currentModel = imageModel;
        String prompt = request.prompt();

        if (prompt.startsWith("/fast")) {
            currentModel = cheapImageModel;
            prompt = prompt.substring(5).trim();
        } else if (prompt.startsWith("/final")) {
            currentModel = unfilteredImageModel;
            prompt = prompt.substring(6).trim();
        }

        try {
            Map<String, Object> body = Map.of(
                "model", currentModel,
                "prompt", prompt
            );

            Map<String, Object> response = restClient.post()
                    .uri("/images/generations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("data")) {
                log.error("Invalid response from OpenRouter image generation: No 'data' field");
                return ImageGenerationResponse.error("AI provider returned an invalid image response.");
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            if (data.isEmpty()) {
                log.error("Invalid response from OpenRouter image generation: 'data' array is empty");
                return ImageGenerationResponse.error("AI provider returned no image data.");
            }
            
            String imageUrl = (String) data.get(0).get("url");

            return ImageGenerationResponse.success(imageUrl, "openrouter-image", request.safetyLevel(), currentModel);

        } catch (HttpClientErrorException e) {
            log.error("HTTP Error calling OpenRouter API for image generation: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ImageGenerationResponse.error("AI Provider for images is facing issues (HTTP Error).");
        } catch (Exception e) {
            log.error("Error calling OpenRouter API for image generation: {}", e.getMessage(), e);
            return ImageGenerationResponse.error("An unexpected error occurred during image generation.");
        }
    }

    @Override
    public boolean isAvailable() {
        return !apiKey.isBlank();
    }
}
