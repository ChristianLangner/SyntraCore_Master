// Autor: Christian Langner
package com.ayntracore.core.application;

import com.ayntracore.core.domain.AppType;
import com.ayntracore.core.domain.Persona;
import com.ayntracore.core.domain.PersonaType;
import com.ayntracore.core.ports.PersonaOutputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Profile("home")
@RequiredArgsConstructor
@Slf4j
public class PersonaService {

    private final PersonaOutputPort personaOutputPort;

    public Persona loadAppPreset(AppType type) {
        return switch (type) {
            case RPG -> createRpgPreset();
            case IHK_EXPERT -> createIhkExpertPreset();
            case DEV_ONBOARDING -> createDevOnboardingPreset();
        };
    }

    private Persona createRpgPreset() {
        return Persona.builder()
                .personaType(PersonaType.COMPANION)
                .speakingStyle("emotional")
                .traits(Map.of("imageGeneration", "true"))
                .build();
    }

    private Persona createIhkExpertPreset() {
        return Persona.builder()
                .personaType(PersonaType.SUPPORT)
                .speakingStyle("professional")
                .systemPrompt("Focus on architecture and technical accuracy.")
                .build();
    }

    private Persona createDevOnboardingPreset() {
        return Persona.builder()
                .personaType(PersonaType.SUPPORT)
                .speakingStyle("supportive")
                .systemPrompt("Assist new developers with codebase and processes.")
                .build();
    }

    public Optional<Persona> getActivePersona(UUID companyId) {
        log.debug("Loading active persona for company: {}", companyId);
        return personaOutputPort.findActiveByCompanyId(companyId);
    }

    public Optional<Persona> getPersonaById(UUID personaId) {
        log.debug("Loading persona by id: {}", personaId);
        return personaOutputPort.findById(personaId);
    }

    public Persona createPersona(
            UUID companyId,
            String name,
            PersonaType personaType,
            String systemPrompt,
            String speakingStyle
    ) {
        log.info("Creating new persona '{}' of type {} for company: {}", name, personaType, companyId);

        Persona persona = Persona.builder()
                .id(UUID.randomUUID())
                .companyId(companyId)
                .name(name)
                .personaType(personaType)
                .systemPrompt(systemPrompt)
                .speakingStyle(speakingStyle)
                .active(true)
                .traits(Map.of("primaryColor", "#FFA500")) // Add orange branding trait
                .build();

        if (personaType == PersonaType.SUPPORT) {
            persona.setAllowExplicitContent(false);
        }

        return personaOutputPort.save(persona);
    }

    public Persona switchPersonaType(UUID personaId, PersonaType newType) {
        log.info("Switching persona {} to type: {}", personaId, newType);

        Persona persona = personaOutputPort.findById(personaId)
                .orElseThrow(() -> new IllegalArgumentException("Persona not found: " + personaId));

        PersonaType oldType = persona.getPersonaType();

        if (oldType == newType) {
            log.debug("Persona type already set to: {}", newType);
            return persona;
        }

        persona.setPersonaType(newType);

        if (newType == PersonaType.SUPPORT) {
            log.info("Switching to SUPPORT mode - disabling explicit content");
            persona.setAllowExplicitContent(false);
        }

        if (oldType == PersonaType.SUPPORT && newType == PersonaType.COMPANION) {
            log.info("Switching from SUPPORT to COMPANION - keeping explicit content disabled (must be enabled manually)");
            persona.setAllowExplicitContent(false);
        }

        return personaOutputPort.save(persona);
    }

    public Persona updateContentPolicy(UUID personaId, boolean allowExplicitContent) {
        log.info("Updating content policy for persona {}: allowExplicitContent={}", personaId, allowExplicitContent);

        Persona persona = personaOutputPort.findById(personaId)
                .orElseThrow(() -> new IllegalArgumentException("Persona not found: " + personaId));

        if (persona.getPersonaType() == PersonaType.SUPPORT && allowExplicitContent) {
            throw new IllegalStateException(
                    "Cannot enable explicit content for SUPPORT persona. Switch to COMPANION type first."
            );
        }

        persona.setAllowExplicitContent(allowExplicitContent);

        return personaOutputPort.save(persona);
    }

    public Persona updatePromptConfiguration(
            UUID personaId,
            String systemPrompt,
            String speakingStyle,
            String promptTemplate,
            String exampleDialog
    ) {
        log.info("Updating prompt configuration for persona: {}", personaId);

        Persona persona = personaOutputPort.findById(personaId)
                .orElseThrow(() -> new IllegalArgumentException("Persona not found: " + personaId));

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            persona.setSystemPrompt(systemPrompt);
        }

        if (speakingStyle != null && !speakingStyle.isBlank()) {
            persona.setSpeakingStyle(speakingStyle);
        }

        if (promptTemplate != null && !promptTemplate.isBlank()) {
            persona.setPromptTemplate(promptTemplate);
        }

        if (exampleDialog != null && !exampleDialog.isBlank()) {
            persona.setExampleDialog(exampleDialog);
        } else if (exampleDialog != null) {
            persona.setExampleDialog(null);
        }

        return personaOutputPort.save(persona);
    }

    public boolean deletePersona(UUID personaId) {
        log.info("Deleting persona: {}", personaId);
        return personaOutputPort.deleteById(personaId);
    }

    public boolean canGenerateExplicitContent(UUID personaId) {
        Persona persona = personaOutputPort.findById(personaId)
                .orElseThrow(() -> new IllegalArgumentException("Persona not found: " + personaId));

        return persona.canGenerateExplicitContent();
    }
}
