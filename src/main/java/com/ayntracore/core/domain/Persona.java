// Autor: Christian Langner
package com.ayntracore.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Domain-Modell für eine Persona (Bot-Charakter) pro Company/Mandant.
 *
 * <p><strong>Architektur-Schicht:</strong> Domain-Modell (Hexagonal Core)</p>
 * <p><strong>Framework-Unabhängigkeit:</strong> Keine Spring/JPA-Abhängigkeiten</p>
 *
 * <h2>Persona-Konzept:</h2>
 * <p>Eine Persona definiert den Charakter und das Verhalten eines Bots.
 * Jede Company kann ihre eigene Persona konfigurieren, um eine markenspezifische
 * Kommunikation zu gewährleisten.</p>
 *
 * <h2>Flexibilität:</h2>
 * <ul>
 *   <li><strong>traits:</strong> Frei erweiterbare Attribute (als JSON im Admin-UI)</li>
 *   <li><strong>promptTemplate:</strong> Template mit Platzhaltern für dynamische Prompts</li>
 *   <li><strong>exampleDialog:</strong> Optionales Few-Shot Beispiel (neutral)</li>
 * </ul>
 *
 * @author Christian Langner
 * @version 2.0
 * @see PersonaType
 * @since 2026
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Persona {

    private UUID id;
    private UUID companyId;

    private String name;

    /**
     * Typ der Persona (SUPPORT oder COMPANION)
     */
    private PersonaType personaType;

    /**
     * Erlaubt expliziten Content (nur für COMPANION relevant)
     */
    private Boolean allowExplicitContent = false;

    /**
     * Basis-Identität
     */
    private String systemPrompt;

    /**
     * Tonalität / Stil
     */
    private String speakingStyle;

    /**
     * Frei erweiterbare Eigenschaften (z.B. avatarUrl, role, responseFormat, brandVoice, ...)
     */
    private Map<String, String> traits = new LinkedHashMap<>();

    /**
     * Template zur Prompt-Erzeugung.
     * Platzhalter: {{systemPrompt}}, {{speakingStyle}}, {{name}}, {{traits}}, {{context}}
     */
    private String promptTemplate = defaultTemplate();

    /**
     * Optionales Beispiel (neutral)
     */
    private String exampleDialog;

    public Persona(UUID companyId, String name, String systemPrompt, String speakingStyle) {
        this.id = UUID.randomUUID();
        this.companyId = companyId;
        this.name = name;
        this.personaType = PersonaType.SUPPORT;
        this.allowExplicitContent = false;
        this.systemPrompt = systemPrompt;
        this.speakingStyle = speakingStyle;
        this.traits = new LinkedHashMap<>();
        this.promptTemplate = defaultTemplate();
        this.exampleDialog = null;
    }

    public Persona(UUID companyId, String name, PersonaType personaType, String systemPrompt, String speakingStyle) {
        this.id = UUID.randomUUID();
        this.companyId = companyId;
        this.name = name;
        this.personaType = personaType;
        this.allowExplicitContent = false;
        this.systemPrompt = systemPrompt;
        this.speakingStyle = speakingStyle;
        this.traits = new LinkedHashMap<>();
        this.promptTemplate = defaultTemplate();
        this.exampleDialog = null;
    }

    public static String defaultTemplate() {
        return ""
                + "{{systemPrompt}}\n"
                + "\nPersona: {{name}}\n"
                + "Speaking Style: {{speakingStyle}}\n"
                + "\nTraits:\n{{traits}}\n"
                + "\nContext:\n{{context}}\n"
                + "\nRegeln: Antworte hilfreich. Stelle Rückfragen bei Unklarheit. Erfinde nichts.";
    }

    /**
     * Prüft, ob die Persona expliziten Content generieren darf.
     * Nur relevant für COMPANION-Personas.
     */
    public boolean canGenerateExplicitContent() {
        return personaType == PersonaType.COMPANION && Boolean.TRUE.equals(allowExplicitContent);
    }
}
