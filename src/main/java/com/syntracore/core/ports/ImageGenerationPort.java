// Autor: Christian Langner
package com.syntracore.core.ports;

import java.util.UUID;

/**
 * Outbound Port für Bildgenerierung (z.B. Civitai API).
 *
 * <p><strong>Architektur-Schicht:</strong> Port (Hexagonal Core)</p>
 * <p><strong>Framework-Unabhängigkeit:</strong> Keine Spring-Abhängigkeiten</p>
 *
 * <h2>Hexagonale Architektur:</h2>
 * <p>Dieser Port definiert die Schnittstelle für externe Bildgenerierungs-Dienste.
 * Die konkrete Implementierung erfolgt durch einen Adapter (z.B. CivitaiAdapter).</p>
 *
 * <h2>Safety-Level (Freizügigkeitsgrad):</h2>
 * <p>Steuert den Grad der erlaubten Freizügigkeit bei der Bildgenerierung.
 * Wird mit der Persona-Konfiguration (allowExplicitContent) kombiniert.</p>
 *
 * @author Christian Langner
 * @version 1.0
 * @since 2026
 *
 * @see com.syntracore.core.domain.Persona
 * @see SafetyLevel
 */
public interface ImageGenerationPort {

    /**
     * Generiert ein Bild basierend auf einem Prompt.
     *
     * @param request Die Bildgenerierungs-Anfrage mit allen Parametern
     * @return Die Antwort mit Bild-URL und Metadaten
     * @throws ImageGenerationException bei Fehlern in der Generierung
     */
    ImageGenerationResponse generateImage(ImageGenerationRequest request);

    /**
     * Prüft, ob der Service verfügbar ist.
     *
     * @return true, wenn der Service erreichbar ist
     */
    boolean isAvailable();

    /**
     * Request-Objekt für Bildgenerierung.
     */
    record ImageGenerationRequest(
            String prompt,
            SafetyLevel safetyLevel,
            UUID companyId,
            String negativePrompt,
            Integer width,
            Integer height,
            Integer steps,
            String model
    ) {
        /**
         * Standard-Konstruktor mit Defaults.
         *
         * @param prompt Der Bildgenerierungs-Prompt
         * @param safetyLevel Der Safety-Level (SAFE, MILD, MODERATE, EXPLICIT, HARDCORE)
         * @param companyId Die Company-ID für Tracking
         */
        public ImageGenerationRequest(String prompt, SafetyLevel safetyLevel, UUID companyId) {
            this(
                    prompt,
                    safetyLevel,
                    companyId,
                    "low quality, blurry, distorted", // Default negative prompt
                    512, // Default width
                    512, // Default height
                    30,  // Default steps
                    null // Default model (wird vom Adapter gewählt)
            );
        }
    }

    /**
     * Response-Objekt für Bildgenerierung.
     */
    record ImageGenerationResponse(
            String imageUrl,
            String imageId,
            SafetyLevel usedSafetyLevel,
            String model,
            boolean success,
            String errorMessage
    ) {
        /**
         * Erfolgreiche Response.
         */
        public static ImageGenerationResponse success(
                String imageUrl,
                String imageId,
                SafetyLevel usedSafetyLevel,
                String model
        ) {
            return new ImageGenerationResponse(imageUrl, imageId, usedSafetyLevel, model, true, null);
        }

        /**
         * Fehler-Response.
         */
        public static ImageGenerationResponse error(String errorMessage) {
            return new ImageGenerationResponse(null, null, null, null, false, errorMessage);
        }
    }

    /**
     * Safety-Level für Bildgenerierung.
     * Definiert den Grad der erlaubten Freizügigkeit.
     */
    enum SafetyLevel {
        /**
         * Komplett sicher, keine Freizügigkeit.
         * Geeignet für: SUPPORT-Personas, öffentliche Anwendungen.
         */
        SAFE(0),

        /**
         * Leichte Freizügigkeit (z.B. Bademode).
         * Geeignet für: COMPANION-Personas mit Einschränkungen.
         */
        MILD(1),

        /**
         * Moderate Freizügigkeit (z.B. Dessous).
         * Geeignet für: COMPANION-Personas mit allowExplicitContent=false.
         */
        MODERATE(2),

        /**
         * Explizite Freizügigkeit (z.B. Nacktheit).
         * Geeignet für: COMPANION-Personas mit allowExplicitContent=true.
         */
        EXPLICIT(3),

        /**
         * Maximale Freizügigkeit (keine Einschränkungen).
         * Geeignet für: COMPANION-Personas mit allowExplicitContent=true.
         */
        HARDCORE(4);

        private final int level;

        SafetyLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }

        /**
         * Prüft, ob dieser Level sicherer ist als der gegebene.
         *
         * @param other Der zu vergleichende Level
         * @return true, wenn dieser Level sicherer ist
         */
        public boolean isSaferThan(SafetyLevel other) {
            return this.level < other.level;
        }

        /**
         * Ermittelt den maximalen erlaubten Safety-Level für eine Persona.
         *
         * @param allowExplicitContent Ob expliziter Content erlaubt ist
         * @return Der maximale Safety-Level
         */
        public static SafetyLevel getMaxAllowedLevel(boolean allowExplicitContent) {
            return allowExplicitContent ? HARDCORE : MODERATE;
        }
    }

    /**
     * Exception für Bildgenerierungs-Fehler.
     */
    class ImageGenerationException extends RuntimeException {
        public ImageGenerationException(String message) {
            super(message);
        }

        public ImageGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
