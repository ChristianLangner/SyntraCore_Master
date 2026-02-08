// Autor: Christian Langner
package com.syntracore.core.application;

import com.syntracore.core.domain.Persona;
import com.syntracore.core.domain.PersonaType;
import com.syntracore.core.ports.ImageGenerationPort;
import com.syntracore.core.ports.ImageGenerationPort.ImageGenerationRequest;
import com.syntracore.core.ports.ImageGenerationPort.ImageGenerationResponse;
import com.syntracore.core.ports.ImageGenerationPort.SafetyLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Application Service für Bildgenerierung mit Safety-Checks.
 *
 * <p><strong>Architektur-Schicht:</strong> Application Layer (Use Cases)</p>
 * <p><strong>Hexagonale Architektur:</strong> Nutzt ImageGenerationPort und ContentSafetyService</p>
 *
 * <h2>Verantwortlichkeiten:</h2>
 * <ul>
 *   <li><strong>Safety-Validierung:</strong> Prüfung der Prompts gegen Persona-Policy</li>
 *   <li><strong>Safety-Level-Mapping:</strong> Automatische Bestimmung des erlaubten Levels</li>
 *   <li><strong>Port-Orchestrierung:</strong> Koordination mit ImageGenerationPort (z.B. Civitai)</li>
 *   <li><strong>Business Rules:</strong> Durchsetzung der Persona-Content-Policies</li>
 * </ul>
 *
 * <h2>Safety-Matrix:</h2>
 * <table border="1">
 *   <tr>
 *     <th>Persona-Typ</th>
 *     <th>allowExplicitContent</th>
 *     <th>Max Safety-Level</th>
 *   </tr>
 *   <tr>
 *     <td>SUPPORT</td>
 *     <td>false (immer)</td>
 *     <td>Bildgenerierung blockiert</td>
 *   </tr>
 *   <tr>
 *     <td>COMPANION</td>
 *     <td>false</td>
 *     <td>MODERATE</td>
 *   </tr>
 *   <tr>
 *     <td>COMPANION</td>
 *     <td>true</td>
 *     <td>HARDCORE</td>
 *   </tr>
 * </table>
 *
 * @author Christian Langner
 * @version 1.0
 * @since 2026
 *
 * @see ImageGenerationPort
 * @see ContentSafetyService
 * @see Persona
 */
@Service
@Profile("home")
@RequiredArgsConstructor
@Slf4j
public class ImageGenerationService {

    private final ImageGenerationPort imageGenerationPort;
    private final ContentSafetyService contentSafetyService;

    /**
     * Generiert ein Bild mit automatischer Safety-Level-Bestimmung.
     *
     * <p>Der Safety-Level wird basierend auf der Persona-Policy automatisch ermittelt:
     * <ul>
     *   <li>SUPPORT: Blockiert (keine Bildgenerierung)</li>
     *   <li>COMPANION ohne expliziten Content: MODERATE</li>
     *   <li>COMPANION mit explizitem Content: HARDCORE</li>
     * </ul>
     *
     * @param prompt Der Bildgenerierungs-Prompt
     * @param persona Die aktive Persona
     * @return Die Bildgenerierungs-Response
     */
    public ImageGenerationResponse generateImage(String prompt, Persona persona) {
        log.debug("Generating image for company: {}, persona type: {}", persona.getCompanyId(), persona.getPersonaType());

        // 1. Prompt-Validierung
        ContentSafetyService.ValidationResult validation = contentSafetyService.validateImagePrompt(prompt, persona);

        if (validation.isInvalid()) {
            log.warn("Image prompt validation failed: {}", validation.errorMessage());
            return ImageGenerationResponse.error(validation.errorMessage());
        }

        // 2. Safety-Level ermitteln
        SafetyLevel safetyLevel = determineSafetyLevel(persona);

        log.info("Determined safety level: {} for persona type: {}, allowExplicitContent: {}",
                safetyLevel, persona.getPersonaType(), persona.getAllowExplicitContent());

        // 3. Request erstellen
        ImageGenerationRequest request = new ImageGenerationRequest(
                prompt,
                safetyLevel,
                persona.getCompanyId()
        );

        // 4. Bildgenerierung durchführen
        try {
            ImageGenerationResponse response = imageGenerationPort.generateImage(request);

            if (response.success()) {
                log.info("Image generated successfully for company: {}, imageId: {}",
                        persona.getCompanyId(), response.imageId());
            } else {
                log.warn("Image generation failed for company: {}, error: {}",
                        persona.getCompanyId(), response.errorMessage());
            }

            return response;

        } catch (ImageGenerationPort.ImageGenerationException e) {
            log.error("Image generation exception for company: {}", persona.getCompanyId(), e);
            return ImageGenerationResponse.error("Image generation failed: " + e.getMessage());
        }
    }

    /**
     * Generiert ein Bild mit explizitem Safety-Level.
     *
     * <p><strong>Business Rule:</strong> Der angeforderte Safety-Level muss innerhalb
     * der erlaubten Grenzen der Persona liegen.</p>
     *
     * @param prompt Der Bildgenerierungs-Prompt
     * @param requestedSafetyLevel Der gewünschte Safety-Level
     * @param persona Die aktive Persona
     * @return Die Bildgenerierungs-Response
     */
    public ImageGenerationResponse generateImageWithSafetyLevel(
            String prompt,
            SafetyLevel requestedSafetyLevel,
            Persona persona
    ) {
        log.debug("Generating image with requested safety level: {}", requestedSafetyLevel);

        // 1. Prompt-Validierung
        ContentSafetyService.ValidationResult validation = contentSafetyService.validateImagePrompt(prompt, persona);

        if (validation.isInvalid()) {
            log.warn("Image prompt validation failed: {}", validation.errorMessage());
            return ImageGenerationResponse.error(validation.errorMessage());
        }

        // 2. Safety-Level-Validierung
        SafetyLevel maxAllowedLevel = getMaxAllowedSafetyLevel(persona);

        if (requestedSafetyLevel.getLevel() > maxAllowedLevel.getLevel()) {
            log.warn("Requested safety level {} exceeds max allowed level {} for company: {}",
                    requestedSafetyLevel, maxAllowedLevel, persona.getCompanyId());

            return ImageGenerationResponse.error(
                    String.format("Requested safety level %s is not allowed. Maximum allowed: %s",
                            requestedSafetyLevel, maxAllowedLevel)
            );
        }

        // 3. Request erstellen
        ImageGenerationRequest request = new ImageGenerationRequest(
                prompt,
                requestedSafetyLevel,
                persona.getCompanyId()
        );

        // 4. Bildgenerierung durchführen
        try {
            return imageGenerationPort.generateImage(request);
        } catch (ImageGenerationPort.ImageGenerationException e) {
            log.error("Image generation exception for company: {}", persona.getCompanyId(), e);
            return ImageGenerationResponse.error("Image generation failed: " + e.getMessage());
        }
    }

    /**
     * Generiert ein Bild mit erweiterten Parametern.
     *
     * @param prompt Der Bildgenerierungs-Prompt
     * @param persona Die aktive Persona
     * @param negativePrompt Der negative Prompt (optional)
     * @param width Bildbreite (optional)
     * @param height Bildhöhe (optional)
     * @param steps Generierungs-Steps (optional)
     * @param model Das zu verwendende Modell (optional)
     * @return Die Bildgenerierungs-Response
     */
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

        // 1. Prompt-Validierung
        ContentSafetyService.ValidationResult validation = contentSafetyService.validateImagePrompt(prompt, persona);

        if (validation.isInvalid()) {
            return ImageGenerationResponse.error(validation.errorMessage());
        }

        // 2. Safety-Level ermitteln
        SafetyLevel safetyLevel = determineSafetyLevel(persona);

        // 3. Request mit erweiterten Parametern erstellen
        ImageGenerationRequest request = new ImageGenerationRequest(
                prompt,
                safetyLevel,
                persona.getCompanyId(),
                negativePrompt != null ? negativePrompt : "low quality, blurry, distorted",
                width != null ? width : 512,
                height != null ? height : 512,
                steps != null ? steps : 30,
                model
        );

        // 4. Bildgenerierung durchführen
        try {
            return imageGenerationPort.generateImage(request);
        } catch (ImageGenerationPort.ImageGenerationException e) {
            log.error("Image generation exception for company: {}", persona.getCompanyId(), e);
            return ImageGenerationResponse.error("Image generation failed: " + e.getMessage());
        }
    }

    /**
     * Prüft, ob die Bildgenerierung für eine Persona verfügbar ist.
     *
     * @param persona Die zu prüfende Persona
     * @return true, wenn Bildgenerierung erlaubt ist
     */
    public boolean isImageGenerationAvailable(Persona persona) {
        // SUPPORT-Personas: Keine Bildgenerierung
        if (persona.getPersonaType() == PersonaType.SUPPORT) {
            return false;
        }

        // COMPANION-Personas: Bildgenerierung erlaubt
        // Service-Verfügbarkeit prüfen
        return imageGenerationPort.isAvailable();
    }

    /**
     * Ermittelt den maximalen erlaubten Safety-Level für eine Persona.
     *
     * @param persona Die Persona
     * @return Der maximale Safety-Level
     */
    public SafetyLevel getMaxAllowedSafetyLevel(Persona persona) {
        // SUPPORT: Keine Bildgenerierung (SAFE als Default)
        if (persona.getPersonaType() == PersonaType.SUPPORT) {
            return SafetyLevel.SAFE;
        }

        // COMPANION: Basierend auf allowExplicitContent
        return SafetyLevel.getMaxAllowedLevel(persona.canGenerateExplicitContent());
    }

    /**
     * Bestimmt den automatischen Safety-Level basierend auf Persona-Policy.
     *
     * @param persona Die Persona
     * @return Der zu verwendende Safety-Level
     */
    private SafetyLevel determineSafetyLevel(Persona persona) {
        if (persona.getPersonaType() == PersonaType.SUPPORT) {
            return SafetyLevel.SAFE;
        }

        if (persona.getPersonaType() == PersonaType.COMPANION) {
            if (persona.canGenerateExplicitContent()) {
                // COMPANION mit explizitem Content: Moderate Level als Standard (nicht direkt Hardcore)
                return SafetyLevel.MODERATE;
            } else {
                // COMPANION ohne expliziten Content: Mild Level
                return SafetyLevel.MILD;
            }
        }

        // Fallback
        return SafetyLevel.SAFE;
    }
}
