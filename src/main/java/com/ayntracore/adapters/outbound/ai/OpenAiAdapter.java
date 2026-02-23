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
                         @Value("${ai.model.chat}") String model,
                         @Value("${ai.model.embedding}") String embeddingModel,
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

            try {
                log.info("FINAL PAYLOAD: {}", objectMapper.writeValueAsString(requestBody));
            } catch (JsonProcessingException e) {
                log.error("Error serializing request body for logging", e);
            }

            Map<String, Object> response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
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

        } catch (HttpClientErrorException e) {
            log.error("Error creating OpenRouter embedding: {}", e.getMessage());
            log.warn("Astra will answer without vector search.");
            return new PGvector(new float[0]);
        } catch (Exception e) {
            log.error("An unexpected error occurred during embedding: {}", e.getMessage());
            log.warn("Returning empty vector.");
            return new PGvector(new float[0]);
        }
    }

    private List<Map<String, Object>> createMessages(AiChatRequest request) {
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // Hard-Coded System-Role
        if (request.systemPrompt() != null && !request.systemPrompt().isBlank()) {
            messages.add(Map.of("role", "system", "content", request.systemPrompt()));
        }
        
        // Add previous messages here
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            messages.addAll(request.getMessages());
        }
        
        messages.add(Map.of("role", "user", "content", request.userMessage()));
        
        // Prompt-Priming
        messages.add(Map.of("role", "assistant", "content", "Astra (zynisch):"));
        
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
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> body = new HashMap<>();
            body.put("model", currentModel);
            body.put("messages", List.of(message));
            body.put("modalities", List.of("image"));

            if (currentModel.equals(unfilteredImageModel) && request.fixedSeed() != null) {
                 body.put("seed", request.fixedSeed());
            }

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
            Map<String, Object> responseMessage = (Map<String, Object>) firstChoice.get("message");
            String base64Image = (String) responseMessage.get("content");

            String imageUrl = "data:image/png;base64," + base64Image;

            return ImageGenerationResponse.success(imageUrl, "openrouter-image", request.safetyLevel(), currentModel);

        } catch (Exception e) {
            log.error("Error calling OpenRouter API for image generation: {}", e.getMessage());
            throw new RuntimeException("AI Provider currently unavailable for image generation (OpenRouter)", e);
        }
    }

    @Override
    public boolean isAvailable() {
        return !apiKey.isBlank();
    }
}
