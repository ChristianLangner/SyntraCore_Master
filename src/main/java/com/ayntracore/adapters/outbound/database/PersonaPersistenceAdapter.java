// Autor: Christian Langner
package com.ayntracore.adapters.outbound.database;

import com.ayntracore.core.domain.Persona;
import com.ayntracore.core.domain.PersonaType;
import com.ayntracore.core.ports.PersonaOutputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Adapter für Persona-Persistierung.
 *
 * <p><strong>Architektur-Schicht:</strong> Infrastructure Layer (Outbound Adapter)</p>
 * <p><strong>Hexagonale Architektur:</strong> Implementiert PersonaOutputPort</p>
 *
 * <h2>Verantwortlichkeiten:</h2>
 * <ul>
 *   <li><strong>Mapping:</strong> Konvertierung zwischen Domain-Modell und JPA-Entity</li>
 *   <li><strong>Persistierung:</strong> CRUD-Operationen via Spring Data JPA</li>
 *   <li><strong>Isolation:</strong> Domain-Schicht bleibt frei von JPA-Annotationen</li>
 * </ul>
 *
 * <h2>Anti-Corruption Layer:</h2>
 * <p>Dieser Adapter fungiert als Anti-Corruption Layer zwischen Domain und Infrastruktur.
 * Die Domain-Modelle (Persona) bleiben rein und frei von technischen Abhängigkeiten.
 * Die JPA-Entities (PersonaJpaEntity) sind rein infrastrukturell.</p>
 *
 * <h2>Mapping-Strategie:</h2>
 * <ul>
 *   <li><strong>Domain → Entity:</strong> Alle Felder werden explizit gemappt</li>
 *   <li><strong>Entity → Domain:</strong> JPA-spezifische Felder (timestamps) werden ignoriert</li>
 *   <li><strong>Traits:</strong> Map&lt;String, String&gt; wird direkt gemappt (JSONB in DB)</li>
 * </ul>
 *
 * @author Christian Langner
 * @version 1.0
 * @since 2026
 *
 * @see PersonaOutputPort
 * @see Persona
 * @see PersonaJpaEntity
 */
@Component
@Profile("home")
@RequiredArgsConstructor
@Slf4j
public class PersonaPersistenceAdapter implements PersonaOutputPort {

    private final SpringDataPersonaRepository personaRepository;

    @Override
    public Optional<Persona> findActiveByCompanyId(UUID companyId) {
        log.debug("Finding active persona for company: {}", companyId);

        return personaRepository.findByCompanyId(companyId)
                .map(this::mapToDomain);
    }

    @Override
    public Persona save(Persona persona) {
        log.info("Saving persona: id={}, companyId={}, type={}",
                persona.getId(), persona.getCompanyId(), persona.getPersonaType());

        // Domain → Entity
        PersonaJpaEntity entity = mapToEntity(persona);

        // JPA-Save
        PersonaJpaEntity savedEntity = personaRepository.save(entity);

        // Entity → Domain
        Persona savedPersona = mapToDomain(savedEntity);

        log.debug("Persona saved successfully: id={}", savedPersona.getId());
        return savedPersona;
    }

    @Override
    public Optional<Persona> findById(UUID id) {
        log.debug("Finding persona by id: {}", id);

        return personaRepository.findById(id)
                .map(this::mapToDomain);
    }

    @Override
    public boolean deleteById(UUID id) {
        log.info("Deleting persona: id={}", id);

        if (personaRepository.existsById(id)) {
            personaRepository.deleteById(id);
            log.debug("Persona deleted successfully: id={}", id);
            return true;
        }

        log.warn("Persona not found for deletion: id={}", id);
        return false;
    }

    /**
     * Mappt Persona Domain-Modell zu PersonaJpaEntity.
     *
     * @param persona Das Domain-Modell
     * @return Die JPA-Entity
     */
    private PersonaJpaEntity mapToEntity(Persona persona) {
        return PersonaJpaEntity.builder()
                .id(persona.getId())
                .companyId(persona.getCompanyId())
                .name(persona.getName())
                .personaType(persona.getPersonaType())
                .allowExplicitContent(persona.getAllowExplicitContent())
                .systemPrompt(persona.getSystemPrompt())
                .speakingStyle(persona.getSpeakingStyle())
                .traits(persona.getTraits())
                .promptTemplate(persona.getPromptTemplate())
                .exampleDialog(persona.getExampleDialog())
                .visualDna(persona.getVisualDna())
                .fixedSeed(persona.getFixedSeed())
                .build();
    }

    /**
     * Mappt PersonaJpaEntity zu Persona Domain-Modell.
     *
     * @param entity Die JPA-Entity
     * @return Das Domain-Modell
     */
    private Persona mapToDomain(PersonaJpaEntity entity) {
        Persona persona = new Persona();
        persona.setId(entity.getId());
        persona.setCompanyId(entity.getCompanyId());
        persona.setName(entity.getName());
        persona.setPersonaType(entity.getPersonaType() != null ? entity.getPersonaType() : PersonaType.SUPPORT);
        persona.setAllowExplicitContent(entity.getAllowExplicitContent() != null ? entity.getAllowExplicitContent() : false);
        persona.setSystemPrompt(entity.getSystemPrompt());
        persona.setSpeakingStyle(entity.getSpeakingStyle());
        persona.setTraits(entity.getTraits());
        persona.setPromptTemplate(entity.getPromptTemplate());
        persona.setExampleDialog(entity.getExampleDialog());
        persona.setVisualDna(entity.getVisualDna());
        persona.setFixedSeed(entity.getFixedSeed());

        return persona;
    }
}
