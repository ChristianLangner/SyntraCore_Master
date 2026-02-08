// Autor: Christian Langner
package com.syntracore.adapters.outbound.civitai;

import com.syntracore.core.ports.ImageGenerationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

/**
 * Outbound Adapter für Civitai Bildgenerierungs-API.
 *
 * <p><strong>Architektur-Schicht:</strong> Infrastructure Layer (Outbound Adapter)</p>
 * <p><strong>Hexagonale Architektur:</strong> Implementiert ImageGenerationPort</p>
 *
 * <h2>API-Integration:</h2>
 * <ul>
 *   <li><strong>Provider:</strong> Civitai AI (https://civitai.com/)</li>
 *   <li><strong>Model:</strong> RealVisXL V4.0 (ID: 139562)</li>
 *   <li><strong>Endpoint:</strong> /api/v1/image/generate</li>
 *   <li><strong>Authentication:</strong> Bearer Token via CIVITAI_API_KEY</li>
 * </ul>
 *
 * <h2>Safety-Level Mapping:</h2>
 * <table border="1">
 *   <tr>
 *     <th>Internal SafetyLevel</th>
 *     <th>Civitai nsfw Parameter</th>
 *     <th>Description</th>
 *   </tr>
 *   <tr>
 *     <td>SAFE</td>
 *     <td>false</td>
 *     <td>Komplett sicher, keine Freizügigkeit</td>
 *   </tr>
 *   <tr>
 *     <td>MILD</td>
 *     <td>false</td>
 *     <td>Leichte Freizügigkeit (Bademode)</td>
 *   </tr>
 *   <tr>
 *     <td>MODERATE</td>
 *     <td>true (mit Safety-Prompt)</td>
 *     <td>Moderate Freizügigkeit (Dessous)</td>
 *   </tr>
 *   <tr>
 *     <td>EXPLICIT</td>
 *     <td>true</td>
 *     <td>Explizite Freizügigkeit</td>
 *   </tr>
 *   <tr>
 *     <td>HARDCORE</td>
 *     <td>true</td>
 *     <td>Maximale Freizügigkeit</td>
 *   </tr>
 * </table>
 *
 * <h2>Error Handling:</h2>
 * <ul>
 *   <li><strong>Timeout:</strong> 60 Sekunden (Bildgenerierung dauert 10-30s)</li>
 *   <li><strong>Retry:</strong> Keine automatischen Retries (App-Layer-Entscheidung)</li>
 *   <li><strong>Fallback:</strong> Error-Response mit Fehlermeldung</li>
 * </ul>
 *
 * @author Christian Langner
 * @version 1.0
 * @since 2026
 *
 * @see ImageGenerationPort
 */
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

        // 1. API-Key-Validierung
        if (apiKey == null || apiKey.isBlank() || "__SET_ME__".equals(apiKey)) {
            log.error("Civitai API key not configured");
            return ImageGenerationResponse.error("Civitai API is not configured. Please set CIVITAI_API_KEY.");
        }

        // 2. Safety-Level-Mapping
        boolean nsfwEnabled = mapSafetyLevelToNsfw(request.safetyLevel());
        String enhancedPrompt = enhancePromptForSafety(request.prompt(), request.safetyLevel());
        String enhancedNegativePrompt = enhanceNegativePrompt(request.negativePrompt(), request.safetyLevel());

        log.debug("NSFW enabled: {}, Enhanced prompt length: {}", nsfwEnabled, enhancedPrompt.length());

        // 3. Request-Body erstellen
        Map<String, Object> requestBody = Map.of(
                "model", request.model() != null ? request.model() : MODEL_ID,
                "prompt", enhancedPrompt,
                "negativePrompt", enhancedNegativePrompt,
                "width", request.width(),
                "height", request.height(),
                "steps", request.steps(),
                "nsfw", nsfwEnabled
        );

        // 4. API-Aufruf
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

            // 5. Response-Verarbeitung
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
            // Health-Check: Einfacher GET auf API-Endpoint
            // Note: Civitai hat keinen dedizierten /health endpoint
            // Alternative: Prüfe nur API-Key-Vorhandensein
            return true;

        } catch (Exception e) {
            log.warn("Civitai API health check failed", e);
            return false;
        }
    }

    /**
     * Mappt interne Safety-Levels auf Civitai NSFW-Parameter.
     *
     * @param safetyLevel Das interne Safety-Level
     * @return true, wenn NSFW-Content erlaubt ist
     */
    private boolean mapSafetyLevelToNsfw(SafetyLevel safetyLevel) {
        return switch (safetyLevel) {
            case SAFE, MILD -> false;
            case MODERATE, EXPLICIT, HARDCORE -> true;
        };
    }

    /**
     * Erweitert den Prompt basierend auf Safety-Level.
     *
     * @param originalPrompt Der Original-Prompt
     * @param safetyLevel Das Safety-Level
     * @return Der erweiterte Prompt
     */
    private String enhancePromptForSafety(String originalPrompt, SafetyLevel safetyLevel) {
        return switch (safetyLevel) {
            case SAFE -> originalPrompt + ", safe for work, professional, appropriate";
            case MILD -> originalPrompt + ", tasteful, elegant, artistic";
            case MODERATE -> originalPrompt + ", artistic, aesthetic";
            case EXPLICIT, HARDCORE -> originalPrompt; // Keine Einschränkungen
        };
    }

    /**
     * Erweitert den Negative-Prompt basierend auf Safety-Level.
     *
     * @param originalNegativePrompt Der Original-Negative-Prompt
     * @param safetyLevel Das Safety-Level
     * @return Der erweiterte Negative-Prompt
     */
    private String enhanceNegativePrompt(String originalNegativePrompt, SafetyLevel safetyLevel) {
        String baseNegative = originalNegativePrompt != null ? originalNegativePrompt : "";

        return switch (safetyLevel) {
            case SAFE -> baseNegative + ", nsfw, nude, explicit, sexual, inappropriate";
            case MILD -> baseNegative + ", explicit, sexual, inappropriate";
            case MODERATE -> baseNegative + ", extreme, hardcore";
            case EXPLICIT, HARDCORE -> baseNegative; // Keine zusätzlichen Einschränkungen
        };
    }
}
