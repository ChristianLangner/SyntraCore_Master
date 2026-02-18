package com.ayntracore.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Persona {

    private UUID id;
    private UUID companyId;
    private String name;
    private PersonaType personaType;
    private boolean active;
    private Boolean allowExplicitContent = false;
    private String systemPrompt;
    private String speakingStyle;
    @Builder.Default
    private Map<String, String> traits = new LinkedHashMap<>();
    @Builder.Default
    private String promptTemplate = defaultTemplate();
    private String exampleDialog;

    public static String defaultTemplate() {
        return ""
                + "{{systemPrompt}}\n"
                + "\nPersona: {{name}}\n"
                + "Speaking Style: {{speakingStyle}}\n"
                + "\nTraits:\n{{traits}}\n"
                + "\nContext:\n{{context}}\n"
                + "\nRegeln: Antworte hilfreich. Stelle Rückfragen bei Unklarheit. Erfinde nichts.";
    }

    public boolean canGenerateExplicitContent() {
        return personaType == PersonaType.COMPANION && Boolean.TRUE.equals(allowExplicitContent);
    }
}
