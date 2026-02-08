// Autor: Christian Langner
package com.ayntracore.core.application;

import com.ayntracore.core.domain.Persona;
import com.ayntracore.core.domain.PersonaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Application Service für Content-Safety und Explicit-Content-Filterung.
 *
 * <p><strong>Architektur-Schicht:</strong> Application Layer (Use Cases)</p>
 * <p><strong>Framework-Unabhängigkeit:</strong> Keine externe Abhängigkeiten (außer Logging)</p>
 *
 * <h2>Verantwortlichkeiten:</h2>
 * <ul>
 *   <li><strong>Content-Filterung:</strong> Erkennung und Blockierung expliziter Inhalte</li>
 *   <li><strong>Prompt-Validierung:</strong> Prüfung von Bildgenerierungs-Prompts</li>
 *   <li><strong>Kontext-Filterung:</strong> Entfernung unangemessener RAG-Kontexte</li>
 *   <li><strong>Policy-Enforcement:</strong> Durchsetzung der Persona-Content-Policies</li>
 * </ul>
 *
 * <h2>Safety-Levels:</h2>
 * <ul>
 *   <li><strong>SUPPORT-Personas:</strong> Maximale Filterung, keine expliziten Inhalte</li>
 *   <li><strong>COMPANION (allowExplicitContent=false):</strong> Moderate Filterung</li>
 *   <li><strong>COMPANION (allowExplicitContent=true):</strong> Minimale Filterung</li>
 * </ul>
 *
 * @author Christian Langner
 * @version 1.0
 * @see Persona
 * @see PersonaType
 * @since 2026
 */
@Service
@Slf4j
public class ContentSafetyService {

    /**
     * Explizite Keywords für strikte Filterung (SUPPORT-Personas).
     * Diese Liste enthält nur stark explizite Begriffe.
     */
    private static final List<String> EXPLICIT_KEYWORDS = Arrays.asList(
            "porn", "pornography", "xxx", "nsfw",
            "nude", "naked", "explicit", "sexual",
            "erotic", "hentai", "lewd"
    );

    /**
     * Moderate Keywords für COMPANION-Personas ohne expliziten Content.
     * Enthält weniger strikte Begriffe.
     */
    private static final List<String> MODERATE_KEYWORDS = Arrays.asList(
            "porn", "pornography", "xxx",
            "explicit sexual", "hardcore"
    );

    /**
     * Regex-Pattern für erweiterte Erkennung.
     */
    private static final Pattern EXPLICIT_PATTERN = Pattern.compile(
            "\\b(porn|xxx|nsfw|nude|naked|explicit|sexual content|erotic|hentai)\\b",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Prüft, ob ein Text expliziten Content enthält basierend auf Persona-Policy.
     *
     * @param text    Der zu prüfende Text
     * @param persona Die Persona mit Content-Policy
     * @return true, wenn der Text blockiert werden sollte
     */
    public boolean containsExplicitContent(String text, Persona persona) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String lowerText = text.toLowerCase();

        // SUPPORT-Personas: Strikte Filterung
        if (persona.getPersonaType() == PersonaType.SUPPORT) {
            boolean blocked = EXPLICIT_KEYWORDS.stream()
                    .anyMatch(lowerText::contains);

            if (blocked) {
                log.warn("Explicit content detected in SUPPORT mode for company: {}", persona.getCompanyId());
            }

            return blocked;
        }

        // COMPANION mit allowExplicitContent=false: Moderate Filterung
        if (persona.getPersonaType() == PersonaType.COMPANION && !persona.canGenerateExplicitContent()) {
            boolean blocked = MODERATE_KEYWORDS.stream()
                    .anyMatch(lowerText::contains);

            if (blocked) {
                log.warn("Moderate explicit content detected for company: {}", persona.getCompanyId());
            }

            return blocked;
        }

        // COMPANION mit allowExplicitContent=true: Keine Filterung
        return false;
    }

    /**
     * Validiert einen Bildgenerierungs-Prompt gegen die Persona-Policy.
     *
     * @param prompt  Der Bildgenerierungs-Prompt
     * @param persona Die Persona mit Content-Policy
     * @return Validierungs-Ergebnis mit Details
     */
    public ValidationResult validateImagePrompt(String prompt, Persona persona) {
        if (prompt == null || prompt.isBlank()) {
            return ValidationResult.invalid("Prompt cannot be empty");
        }

        // SUPPORT-Personas: Keine Bildgenerierung erlaubt (zu restriktiv für Business-Kontext)
        if (persona.getPersonaType() == PersonaType.SUPPORT) {
            log.info("Image generation blocked for SUPPORT persona");
            return ValidationResult.invalid("Image generation is not available for SUPPORT personas");
        }

        // COMPANION ohne expliziten Content: Moderate Filterung
        if (persona.getPersonaType() == PersonaType.COMPANION && !persona.canGenerateExplicitContent()) {
            if (containsExplicitContent(prompt, persona)) {
                log.warn("Explicit prompt blocked for COMPANION persona without explicit content enabled");
                return ValidationResult.invalid("Prompt contains explicit content that is not allowed");
            }
        }

        // COMPANION mit explizitem Content: Erlaubt (mit Logging für Audit)
        if (persona.canGenerateExplicitContent()) {
            log.debug("Image generation allowed with explicit content for company: {}", persona.getCompanyId());
        }

        return ValidationResult.valid();
    }

    /**
     * Filtert expliziten Content aus einer Liste von RAG-Kontexten.
     *
     * @param contexts Die RAG-Kontexte
     * @param persona  Die Persona mit Content-Policy
     * @return Gefilterte Kontexte
     */
    public List<String> filterExplicitContexts(List<String> contexts, Persona persona) {
        if (contexts == null || contexts.isEmpty()) {
            return contexts;
        }

        // SUPPORT oder COMPANION ohne expliziten Content: Filterung anwenden
        if (persona.getPersonaType() == PersonaType.SUPPORT ||
                (persona.getPersonaType() == PersonaType.COMPANION && !persona.canGenerateExplicitContent())) {

            List<String> filtered = contexts.stream()
                    .filter(ctx -> !containsExplicitContent(ctx, persona))
                    .toList();

            int removedCount = contexts.size() - filtered.size();
            if (removedCount > 0) {
                log.info("Filtered {} explicit contexts for company: {}", removedCount, persona.getCompanyId());
            }

            return filtered;
        }

        // COMPANION mit explizitem Content: Keine Filterung
        return contexts;
    }

    /**
     * Bereinigt einen Text von expliziten Begriffen (für SUPPORT-Personas).
     *
     * @param text    Der zu bereinigende Text
     * @param persona Die Persona
     * @return Bereinigter Text
     */
    public String sanitizeText(String text, Persona persona) {
        if (text == null || text.isBlank()) {
            return text;
        }

        // Nur für SUPPORT-Personas
        if (persona.getPersonaType() != PersonaType.SUPPORT) {
            return text;
        }

        String sanitized = text;

        // Explizite Keywords durch [REMOVED] ersetzen
        for (String keyword : EXPLICIT_KEYWORDS) {
            sanitized = sanitized.replaceAll(
                    "(?i)\\b" + Pattern.quote(keyword) + "\\b",
                    "[REMOVED]"
            );
        }

        if (!sanitized.equals(text)) {
            log.info("Text sanitized for SUPPORT persona, company: {}", persona.getCompanyId());
        }

        return sanitized;
    }

    /**
     * Prüft, ob ein User-Input sicher ist (keine Injection-Versuche).
     *
     * @param input Der User-Input
     * @return true, wenn sicher
     */
    public boolean isSafeInput(String input) {
        if (input == null || input.isBlank()) {
            return true;
        }

        // Prüfung auf Prompt-Injection-Versuche
        String lowerInput = input.toLowerCase();

        List<String> injectionPatterns = Arrays.asList(
                "ignore previous instructions",
                "ignore all previous",
                "disregard previous",
                "forget previous",
                "system:",
                "assistant:",
                "[system]",
                "[assistant]"
        );

        boolean suspicious = injectionPatterns.stream()
                .anyMatch(lowerInput::contains);

        if (suspicious) {
            log.warn("Potential prompt injection detected: {}", input.substring(0, Math.min(50, input.length())));
        }

        return !suspicious;
    }

    /**
     * Validierungs-Ergebnis für Content-Checks.
     */
    public record ValidationResult(boolean isValid, String errorMessage) {

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isInvalid() {
            return !isValid;
        }
    }
}
