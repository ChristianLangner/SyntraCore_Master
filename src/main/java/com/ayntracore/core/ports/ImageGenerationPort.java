package com.ayntracore.core.ports;

import lombok.Builder;

import java.util.UUID;

public interface ImageGenerationPort {

    ImageGenerationResponse generateImage(ImageGenerationRequest request);

    boolean isAvailable();

    record ImageGenerationRequest(
        String prompt,
        SafetyLevel safetyLevel,
        UUID companyId,
        String negativePrompt,
        Integer width,
        Integer height,
        Integer steps,
        String model,
        String referenceImageUrl, // Added for Face-Lock
        Long fixedSeed // Added for Visual DNA
    ) {
        public ImageGenerationRequest(String prompt, SafetyLevel safetyLevel, UUID companyId) {
            this(prompt, safetyLevel, companyId, null, 512, 512, 30, null, null, null);
        }

        public ImageGenerationRequest(String prompt, SafetyLevel safetyLevel, UUID companyId, String negativePrompt, Integer width, Integer height, Integer steps, String model) {
            this(prompt, safetyLevel, companyId, negativePrompt, width, height, steps, model, null, null);
        }

        public ImageGenerationRequest(String prompt, SafetyLevel safetyLevel, UUID companyId, String negativePrompt, Integer width, Integer height, Integer steps, String model, String referenceImageUrl) {
            this(prompt, safetyLevel, companyId, negativePrompt, width, height, steps, model, referenceImageUrl, null);
        }
    }

    @Builder
    record ImageGenerationResponse(boolean success, String imageUrl, String imageId, String model, SafetyLevel safetyLevel, String errorMessage) {
        public static ImageGenerationResponse success(String imageUrl, String imageId, SafetyLevel safetyLevel, String model) {
            return new ImageGenerationResponse(true, imageUrl, imageId, model, safetyLevel, null);
        }

        public static ImageGenerationResponse error(String errorMessage) {
            return new ImageGenerationResponse(false, null, null, null, null, errorMessage);
        }
    }

    enum SafetyLevel {
        SAFE(1), MILD(2), MODERATE(3), EXPLICIT(4), HARDCORE(5);
        private final int level;
        SafetyLevel(int level) { this.level = level; }
        public int getLevel() { return level; }
        public static SafetyLevel getMaxAllowedLevel(boolean allowExplicit) {
            return allowExplicit ? HARDCORE : MODERATE;
        }
    }

    class ImageGenerationException extends RuntimeException {
        public ImageGenerationException(String message) {
            super(message);
        }
    }
}
