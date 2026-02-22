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

import java.util.UUID;

@Service
@Profile("home")
@RequiredArgsConstructor
@Slf4j
public class ImageGenerationService {

    private final ImageGenerationPort imageGenerationPort;
    private final ContentSafetyService contentSafetyService;

    public ImageGenerationResponse generateImage(String prompt, Persona persona) {
        log.debug("Generating image for company: {}, persona type: {}", persona.getCompanyId(), persona.getPersonaType());

        return generateImageAdvanced(prompt, persona, null, null, null, null, null);
    }

    public ImageGenerationResponse generateImageWithSafetyLevel(
            String prompt,
            SafetyLevel requestedSafetyLevel,
            Persona persona
    ) {
        log.debug("Generating image with requested safety level: {}", requestedSafetyLevel);

        ContentSafetyService.ValidationResult validation = contentSafetyService.validateImagePrompt(prompt, persona);

        if (validation.isInvalid()) {
            log.warn("Image prompt validation failed: {}", validation.errorMessage());
            return ImageGenerationResponse.error(validation.errorMessage());
        }

        SafetyLevel maxAllowedLevel = getMaxAllowedSafetyLevel(persona);

        if (requestedSafetyLevel.getLevel() > maxAllowedLevel.getLevel()) {
            log.warn("Requested safety level {} exceeds max allowed level {} for company: {}",
                    requestedSafetyLevel, maxAllowedLevel, persona.getCompanyId());

            return ImageGenerationResponse.error(
                    String.format("Requested safety level %s is not allowed. Maximum allowed: %s",
                            requestedSafetyLevel, maxAllowedLevel)
            );
        }

        ImageGenerationRequest request = new ImageGenerationRequest(
                prompt,
                requestedSafetyLevel,
                persona.getCompanyId()
        );

        try {
            return imageGenerationPort.generateImage(request);
        } catch (ImageGenerationPort.ImageGenerationException e) {
            log.error("Image generation exception for company: {}", persona.getCompanyId(), e);
            return ImageGenerationResponse.error("Image generation failed: " + e.getMessage());
        }
    }

    public ImageGenerationResponse generateImageAdvanced(
            String prompt,
            Persona persona,
            String negativePrompt,
            Integer width,
            Integer height,
            Integer steps,
            String model
    ) {
        log.debug("Generating image with advanced parameters for company: {}", persona.getCompanyId());

        ContentSafetyService.ValidationResult validation = contentSafetyService.validateImagePrompt(prompt, persona);

        if (validation.isInvalid()) {
            return ImageGenerationResponse.error(validation.errorMessage());
        }

        SafetyLevel safetyLevel = determineSafetyLevel(persona);

        String finalPrompt = persona.getVisualDna() != null ? persona.getVisualDna() + ", " + prompt : prompt;

        ImageGenerationRequest request = new ImageGenerationRequest(
                finalPrompt,
                safetyLevel,
                persona.getCompanyId(),
                negativePrompt != null ? negativePrompt : "low quality, blurry, distorted",
                width != null ? width : 512,
                height != null ? height : 512,
                steps != null ? steps : 30,
                model,
                persona.getTraits().get("referenceImageUrl"),
                persona.getFixedSeed()
        );

        try {
            return imageGenerationPort.generateImage(request);
        } catch (ImageGenerationPort.ImageGenerationException e) {
            log.error("Image generation exception for company: {}", persona.getCompanyId(), e);
            return ImageGenerationResponse.error("Image generation failed: " + e.getMessage());
        }
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
}
