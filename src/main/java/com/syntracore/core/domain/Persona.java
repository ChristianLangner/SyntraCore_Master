package com.syntracore.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * // UPDATE #58
 * Domain-Modell für eine Persona (Bot-Charakter) pro Company/Mandant.
 *
 * Flexibel:
 * - traits: frei erweiterbare Attribute (als JSON im Admin-UI)
 * - promptTemplate: Template mit Platzhaltern
 * - exampleDialog: optionales Few-Shot Beispiel (neutral)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Persona {

    private UUID id;
    private UUID companyId;

    private String name;

    /** Basis-Identität */
    private String systemPrompt;

    /** Tonalität / Stil */
    private String speakingStyle;

    /** Frei erweiterbare Eigenschaften (z.B. avatarUrl, role, responseFormat, brandVoice, ...) */
    private Map<String, String> traits = new LinkedHashMap<>();

    /**
     * Template zur Prompt-Erzeugung.
     * Platzhalter: {{systemPrompt}}, {{speakingStyle}}, {{name}}, {{traits}}, {{context}}
     */
    private String promptTemplate = defaultTemplate();

    /** Optionales Beispiel (neutral) */
    private String exampleDialog;

    public Persona(UUID companyId, String name, String systemPrompt, String speakingStyle) {
        this.id = UUID.randomUUID();
        this.companyId = companyId;
        this.name = name;
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
}