package com.ayntracore.adapters.outbound.civitai;

import com.ayntracore.core.ports.ImageGenerationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("home")
@Slf4j
public class CivitaiImageAdapter implements ImageGenerationPort {

    private static final String CIVITAI_API_URL = "https://api.civitai.com";
    private static final String MODEL_ID = "139562"; // RealVisXL V4.0
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    private final RestClient restClient;
    private final String apiKey;

    public CivitaiImageAdapter(
            RestClient.Builder builder,
            @Value("${civitai.api.key:}") String apiKey
    ) {
        this.apiKey = apiKey;
        this.restClient = builder
                .baseUrl(CIVITAI_API_URL)
                .build();

        log.info("CivitaiImageAdapter initialized with model: RealVisXL V4.0 ({})", MODEL_ID);
    }

    @Override
    public ImageGenerationResponse generateImage(ImageGenerationRequest request) {
        log.info("Generating image for company: {}, safetyLevel: {}", request.companyId(), request.safetyLevel());

        if (apiKey == null || apiKey.isBlank() || "__SET_ME__".equals(apiKey)) {
            log.error("Civitai API key not configured");
            return ImageGenerationResponse.error("Civitai API is not configured. Please set CIVITAI_API_KEY.");
        }

        boolean nsfwEnabled = mapSafetyLevelToNsfw(request.safetyLevel());
        String enhancedPrompt = enhancePromptForSafety(request.prompt(), request.safetyLevel());
        String enhancedNegativePrompt = enhanceNegativePrompt(request.negativePrompt(), request.safetyLevel());

        log.debug("NSFW enabled: {}, Enhanced prompt length: {}", nsfwEnabled, enhancedPrompt.length());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", request.model() != null ? request.model() : MODEL_ID);
        requestBody.put("prompt", enhancedPrompt);
        requestBody.put("negativePrompt", enhancedNegativePrompt);
        requestBody.put("width", request.width());
        requestBody.put("height", request.height());
        requestBody.put("steps", request.steps());
        requestBody.put("nsfw", nsfwEnabled);

        if (request.referenceImageUrl() != null && !request.referenceImageUrl().isBlank()) {
            log.info("Applying Face-Lock (IP-Adapter) using reference image: {}", request.referenceImageUrl());
            Map<String, Object> controlNet = Map.of(
                    "model", "IP-Adapter",
                    "controlMode", "Balanced",
                    "imageUrl", request.referenceImageUrl(),
                    "weight", 0.75
            );
            requestBody.put("controlNets", List.of(controlNet));
        }

        try {
            log.debug("Calling Civitai API: POST /api/v1/image/generate");

            Map<String, Object> response = restClient.post()
                    .uri("/api/v1/image/generate")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        log.error("Civitai API client error: {} - {}", res.getStatusCode(), res.getStatusText());
                        throw new ImageGenerationException("Civitai API client error: " + res.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        log.error("Civitai API server error: {} - {}", res.getStatusCode(), res.getStatusText());
                        throw new ImageGenerationException("Civitai API server error: " + res.getStatusCode());
                    })
                    .body(Map.class);

            if (response == null || !response.containsKey("imageUrl")) {
                log.warn("Invalid response from Civitai API: missing imageUrl");
                return ImageGenerationResponse.error("Invalid response from Civitai API");
            }

            String imageUrl = (String) response.get("imageUrl");
            String imageId = response.containsKey("id") ? String.valueOf(response.get("id")) : "unknown";

            log.info("Image generated successfully: imageId={}, url={}", imageId, imageUrl);

            return ImageGenerationResponse.success(
                    imageUrl,
                    imageId,
                    request.safetyLevel(),
                    request.model() != null ? request.model() : MODEL_ID
            );

        } catch (ImageGenerationException e) {
            log.error("Image generation failed: {}", e.getMessage());
            return ImageGenerationResponse.error(e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error during image generation", e);
            return ImageGenerationResponse.error("Unexpected error: " + e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        if (apiKey == null || apiKey.isBlank() || "__SET_ME__".equals(apiKey)) {
            log.debug("Civitai API not available: API key not configured");
            return false;
        }

        try {
            return true;

        } catch (Exception e) {
            log.warn("Civitai API health check failed", e);
            return false;
        }
    }

    private boolean mapSafetyLevelToNsfw(SafetyLevel safetyLevel) {
        return switch (safetyLevel) {
            case SAFE, MILD -> false;
            case MODERATE, EXPLICIT, HARDCORE -> true;
        };
    }

    private String enhancePromptForSafety(String originalPrompt, SafetyLevel safetyLevel) {
        return switch (safetyLevel) {
            case SAFE -> originalPrompt + ", safe for work, professional, appropriate";
            case MILD -> originalPrompt + ", tasteful, elegant, artistic";
            case MODERATE -> originalPrompt + ", artistic, aesthetic";
            case EXPLICIT, HARDCORE -> originalPrompt;
        };
    }

    private String enhanceNegativePrompt(String originalNegativePrompt, SafetyLevel safetyLevel) {
        String baseNegative = originalNegativePrompt != null ? originalNegativePrompt : "";

        return switch (safetyLevel) {
            case SAFE -> baseNegative + ", nsfw, nude, explicit, sexual, inappropriate";
            case MILD -> baseNegative + ", explicit, sexual, inappropriate";
            case MODERATE -> baseNegative + ", extreme, hardcore";
            case EXPLICIT, HARDCORE -> baseNegative;
        };
    }
}
