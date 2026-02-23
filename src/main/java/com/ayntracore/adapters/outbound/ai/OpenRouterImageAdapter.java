package com.ayntracore.adapters.outbound.ai;

import com.ayntracore.core.domain.ImageGenerationResponse;
import com.ayntracore.core.ports.ImageGenerationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@Primary
@ConditionalOnProperty(name = "adapter.image.provider", havingValue = "openrouter", matchIfMissing = true)
@Slf4j
public class OpenRouterImageAdapter implements ImageGenerationPort {

    private final RestClient restClient;

    public OpenRouterImageAdapter(
            RestClient.Builder builder,
            @Value("${openrouter.api.key:}") String apiKey
    ) {
        this.restClient = builder
                .baseUrl("https://openrouter.ai/api/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        log.info("OpenRouterImageAdapter initialized");
    }

    @Override
    public ImageGenerationResponse generateImage(ImageGenerationRequest request) {
        log.info("Generating image with OpenRouter for company: {}", request.companyId());

        // For simplicity, this example uses a DALL-E 3 model via OpenRouter.
        // Note: Check OpenRouter documentation for the correct model identifier and API details.
        String model = "openai/dall-e-3";

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "prompt", request.prompt(),
                "n", 1,
                "size", "1024x1024"
        );

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/images/generations")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("data")) {
                java.util.List<Map<String, Object>> data = (java.util.List<Map<String, Object>>) response.get("data");
                if (data != null && !data.isEmpty()) {
                    String imageUrl = (String) data.get(0).get("url");
                    log.info("Image generated via OpenRouter: {}", imageUrl);
                    return ImageGenerationResponse.success(imageUrl, "openrouter-" + System.currentTimeMillis(), request.safetyLevel(), model);
                }
            }
            return ImageGenerationResponse.error("Invalid response from OpenRouter");

        } catch (Exception e) {
            log.error("Error during OpenRouter image generation", e);
            return ImageGenerationResponse.error(e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        // You can implement a health check here if needed
        return true;
    }
}
