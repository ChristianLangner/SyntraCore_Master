// Autor: Christian Langner
package com.ayntracore.core.domain;

public record ImageGenerationResponse(
    boolean success,
    String imageUrl,
    String revisedPrompt,
    String provider,
    String model,
    String safetyLevel,
    String errorMessage
) {
    public static ImageGenerationResponse success(String imageUrl, String revisedPrompt, String safetyLevel, String model) {
        return new ImageGenerationResponse(true, imageUrl, revisedPrompt, "openrouter", model, safetyLevel, null);
    }

    public static ImageGenerationResponse error(String errorMessage, String model) {
        return new ImageGenerationResponse(false, null, null, "openrouter", model, null, errorMessage);
    }
}
