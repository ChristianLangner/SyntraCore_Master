package com.syntracore.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Domain-Modell für eine Persona (Bot-Charakter) pro Company/Mandant.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Persona {

    private UUID id;
    private UUID companyId;

    private String name;
    private String systemPrompt;
    private String speakingStyle;

    public Persona(UUID companyId, String name, String systemPrompt, String speakingStyle) {
        this.id = UUID.randomUUID();
        this.companyId = companyId;
        this.name = name;
        this.systemPrompt = systemPrompt;
        this.speakingStyle = speakingStyle;
    }
}