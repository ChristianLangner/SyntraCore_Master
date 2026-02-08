// Autor: Christian Langner
package com.syntracore.core.application;

import com.syntracore.core.domain.Persona;
import com.syntracore.core.domain.PersonaType;
import com.syntracore.core.ports.PersonaOutputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Application Service für Persona-Verwaltung und Typ-Wechsel.
 *
 * <p><strong>Architektur-Schicht:</strong> Application Layer (Use Cases)</p>
 * <p><strong>Hexagonale Architektur:</strong> Nutzt Ports, keine direkte DB-Verbindung</p>
 *
 * <h2>Verantwortlichkeiten:</h2>
 * <ul>
 *   <li><strong>Persona-Verwaltung:</strong> Laden, Speichern, Aktualisieren von Personas</li>
 *   <li><strong>Typ-Wechsel:</strong> Wechsel zwischen SUPPORT und COMPANION mit Validierung</li>
 *   <li><strong>Content-Policy:</strong> Verwaltung von allowExplicitContent-Flag</li>
 *   <li><strong>Sicherheit:</strong> Automatische Zurücksetzung bei Typ-Wechsel zu SUPPORT</li>
 * </ul>
 *
 * <h2>Business Rules:</h2>
 * <ul>
 *   <li>SUPPORT-Personas: allowExplicitContent wird automatisch auf false gesetzt</li>
 *   <li>COMPANION-Personas: allowExplicitContent ist konfigurierbar</li>
 *   <li>Typ-Wechsel von COMPANION zu SUPPORT: Automatische Bereinigung der Content-Policy</li>
 * </ul>
 *
 * @author Christian Langner
 * @version 1.0
 * @since 2026
 *
 * @see Persona
 * @see PersonaType
 * @see PersonaOutputPort
 */
@Service
@Profile("home")
@RequiredArgsConstructor
@Slf4j
public class PersonaService {

    private final PersonaOutputPort personaOutputPort;

    /**
     * Lädt die aktive Persona für eine Company.
     *
     * @param companyId Die Company-ID
     * @return Optional mit Persona, falls vorhanden
     */
    public Optional<Persona> getActivePersona(UUID companyId) {
        log.debug("Loading active persona for company: {}", companyId);
        return personaOutputPort.findActiveByCompanyId(companyId);
    }

    /**
     * Lädt eine Persona anhand ihrer ID.
     *
     * @param personaId Die Persona-ID
     * @return Optional mit Persona, falls vorhanden
     */
    public Optional<Persona> getPersonaById(UUID personaId) {
        log.debug("Loading persona by id: {}", personaId);
        return personaOutputPort.findById(personaId);
    }

    /**
     * Erstellt eine neue Persona für eine Company.
     *
     * @param companyId Die Company-ID
     * @param name Der Name der Persona
     * @param personaType Der Typ (SUPPORT oder COMPANION)
     * @param systemPrompt Der System-Prompt
     * @param speakingStyle Der Sprachstil
     * @return Die erstellte Persona
     */
    public Persona createPersona(
            UUID companyId,
            String name,
            PersonaType personaType,
            String systemPrompt,
            String speakingStyle
    ) {
        log.info("Creating new persona '{}' of type {} for company: {}", name, personaType, companyId);

        Persona persona = new Persona(companyId, name, personaType, systemPrompt, speakingStyle);

        // Business Rule: SUPPORT-Personas dürfen keinen expliziten Content generieren
        if (personaType == PersonaType.SUPPORT) {
            persona.setAllowExplicitContent(false);
        }

        return personaOutputPort.save(persona);
    }

    /**
     * Wechselt den Typ einer Persona zwischen SUPPORT und COMPANION.
     *
     * <p><strong>Business Rules:</strong></p>
     * <ul>
     *   <li>Bei Wechsel zu SUPPORT: allowExplicitContent wird automatisch auf false gesetzt</li>
     *   <li>Bei Wechsel zu COMPANION: allowExplicitContent bleibt false (muss explizit aktiviert werden)</li>
     * </ul>
     *
     * @param personaId Die ID der Persona
     * @param newType Der neue Typ
     * @return Die aktualisierte Persona
     * @throws IllegalArgumentException wenn Persona nicht gefunden
     */
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

        // Business Rule: Bei Wechsel zu SUPPORT immer expliziten Content deaktivieren
        if (newType == PersonaType.SUPPORT) {
            log.info("Switching to SUPPORT mode - disabling explicit content");
            persona.setAllowExplicitContent(false);
        }

        // Bei Wechsel von SUPPORT zu COMPANION: Sicherheitshalber auch deaktivieren
        if (oldType == PersonaType.SUPPORT && newType == PersonaType.COMPANION) {
            log.info("Switching from SUPPORT to COMPANION - keeping explicit content disabled (must be enabled manually)");
            persona.setAllowExplicitContent(false);
        }

        return personaOutputPort.save(persona);
    }

    /**
     * Aktualisiert die Content-Policy einer Persona.
     *
     * <p><strong>Business Rule:</strong> Nur COMPANION-Personas dürfen expliziten Content aktivieren</p>
     *
     * @param personaId Die ID der Persona
     * @param allowExplicitContent Ob expliziter Content erlaubt ist
     * @return Die aktualisierte Persona
     * @throws IllegalArgumentException wenn Persona nicht gefunden
     * @throws IllegalStateException wenn SUPPORT-Persona expliziten Content aktivieren soll
     */
    public Persona updateContentPolicy(UUID personaId, boolean allowExplicitContent) {
        log.info("Updating content policy for persona {}: allowExplicitContent={}", personaId, allowExplicitContent);

        Persona persona = personaOutputPort.findById(personaId)
                .orElseThrow(() -> new IllegalArgumentException("Persona not found: " + personaId));

        // Business Rule: SUPPORT-Personas dürfen niemals expliziten Content generieren
        if (persona.getPersonaType() == PersonaType.SUPPORT && allowExplicitContent) {
            throw new IllegalStateException(
                    "Cannot enable explicit content for SUPPORT persona. Switch to COMPANION type first."
            );
        }

        persona.setAllowExplicitContent(allowExplicitContent);

        return personaOutputPort.save(persona);
    }

    /**
     * Aktualisiert die Prompt-Konfiguration einer Persona.
     *
     * @param personaId Die ID der Persona
     * @param systemPrompt Der neue System-Prompt (optional)
     * @param speakingStyle Der neue Sprachstil (optional)
     * @param promptTemplate Das neue Prompt-Template (optional)
     * @param exampleDialog Der neue Beispiel-Dialog (optional)
     * @return Die aktualisierte Persona
     * @throws IllegalArgumentException wenn Persona nicht gefunden
     */
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
            // Explicit null/empty string -> clear example dialog
            persona.setExampleDialog(null);
        }

        return personaOutputPort.save(persona);
    }

    /**
     * Löscht eine Persona.
     *
     * @param personaId Die ID der zu löschenden Persona
     * @return true, wenn erfolgreich gelöscht
     */
    public boolean deletePersona(UUID personaId) {
        log.info("Deleting persona: {}", personaId);
        return personaOutputPort.deleteById(personaId);
    }

    /**
     * Prüft, ob eine Persona expliziten Content generieren darf.
     *
     * @param personaId Die ID der Persona
     * @return true, wenn expliziter Content erlaubt ist
     * @throws IllegalArgumentException wenn Persona nicht gefunden
     */
    public boolean canGenerateExplicitContent(UUID personaId) {
        Persona persona = personaOutputPort.findById(personaId)
                .orElseThrow(() -> new IllegalArgumentException("Persona not found: " + personaId));

        return persona.canGenerateExplicitContent();
    }
}
