// Autor: Christian Langner
package com.ayntracore.core.application;

import com.ayntracore.core.domain.Persona;
import com.ayntracore.core.domain.PersonaType;
import com.ayntracore.core.ports.ImageGenerationPort;
import com.ayntracore.core.ports.ImageGenerationPort.ImageGenerationRequest;
import com.ayntracore.core.ports.ImageGenerationPort.ImageGenerationResponse;
import com.ayntracore.core.ports.ImageGenerationPort.SafetyLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("home")
@RequiredArgsConstructor
@Slf4j
public class ImageGenerationService {

    private final ImageGenerationPort imageGenerationPort;
    private final ContentSafetyService contentSafetyService;

    // Unchanged methods ...
    public ImageGenerationResponse generateImage(String prompt, Persona persona) {
        return generateImageAdvanced(prompt, persona, null, null, null, null, null);
    }
    // ...

    public ImageGenerationResponse generateImageAdvanced(
            String prompt,
            Persona persona,
            String negativePrompt,
            Integer width,
            Integer height,
            Integer steps,
            String model
    ) {
        // Guard Clause: Immediately reject if persona type does not support images.
        if (persona.getPersonaType() == PersonaType.SUPPORT) {
            String errorMessage = String.format("Image generation rejected for persona '%s' because its type is SUPPORT.", persona.getName());
            log.warn(errorMessage);
            return ImageGenerationResponse.error(errorMessage);
        }

        log.debug("Generating image with advanced parameters for company: {}", persona.getCompanyId());

        // This validation is still useful for other checks (e.g. prompt content)
        ContentSafetyService.ValidationResult validation = contentSafetyService.validateImagePrompt(prompt, persona);
        if (validation.isInvalid()) {
            log.warn("Content safety validation failed for image prompt: {}", validation.errorMessage());
            return ImageGenerationResponse.error(validation.errorMessage());
        }

        SafetyLevel safetyLevel = determineSafetyLevel(persona);
        String finalPrompt = persona.getVisualDna() != null ? persona.getVisualDna() + ", " + prompt : prompt;

        ImageGenerationRequest request = new ImageGenerationRequest(
                finalPrompt,
                safetyLevel,
                persona.getCompanyId(),
                negativePrompt != null ? negativePrompt : "low quality, blurry, distorted",
                width != null ? width : 1024,
                height != null ? height : 1024,
                steps != null ? steps : 20,
                model,
                persona.getTraits().get("referenceImageUrl"),
                persona.getFixedSeed()
        );

        try {
            return imageGenerationPort.generateImage(request);
        } catch (ImageGenerationPort.ImageGenerationException e) {
            log.error("Image generation failed for company: {}", persona.getCompanyId(), e);
            return ImageGenerationResponse.error("Image generation failed: " + e.getMessage());
        }
    }
    
    // Unchanged methods ...
    private SafetyLevel determineSafetyLevel(Persona persona) {
        if (persona.getPersonaType() == PersonaType.SUPPORT) {
            return SafetyLevel.SAFE;
        }

        if (persona.getPersonaType() == PersonaType.COMPANION) {
            if (persona.canGenerateExplicitContent()) {
                return SafetyLevel.MODERATE;
            } else {
                return SafetyLevel.MILD;
            }
        }

        return SafetyLevel.SAFE;
    }
    
    public boolean isImageGenerationAvailable(Persona persona) {
        if (persona.getPersonaType() == PersonaType.SUPPORT) {
            return false;
        }
        return imageGenerationPort.isAvailable();
    }

    public SafetyLevel getMaxAllowedSafetyLevel(Persona persona) {
        if (persona.getPersonaType() == PersonaType.SUPPORT) {
            return SafetyLevel.SAFE;
        }
        return SafetyLevel.getMaxAllowedLevel(persona.canGenerateExplicitContent());
    }
    // ...
}
