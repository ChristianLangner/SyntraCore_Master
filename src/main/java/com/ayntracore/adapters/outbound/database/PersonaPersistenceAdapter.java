// Autor: Christian Langner
package com.ayntracore.adapters.outbound.database;

import com.ayntracore.core.domain.Persona;
import com.ayntracore.core.domain.PersonaType;
import com.ayntracore.core.ports.PersonaRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Adapter for Persona persistence.
 *
 * <p><strong>Architecture Layer:</strong> Infrastructure Layer (Outbound Adapter)</p>
 * <p><strong>Hexagonal Architecture:</strong> Implements PersonaRepositoryPort</p>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *   <li><strong>Mapping:</strong> Converts between the domain model (Persona) and the JPA entity (PersonaJpaEntity).</li>
 *   <li><strong>Persistence:</strong> Handles CRUD operations via Spring Data JPA.</li>
 *   <li><strong>Isolation:</strong> The domain layer remains free from JPA annotations and persistence-specific details.</li>
 * </ul>
 *
 * <h2>Anti-Corruption Layer:</h2>
 * <p>This adapter acts as an Anti-Corruption Layer, protecting the domain model from infrastructure concerns.
 * The Persona domain model is pure and technology-agnostic, while the PersonaJpaEntity is a private
 * implementation detail of the persistence layer.</p>
 *
 * <h2>Mapping Strategy:</h2>
 * <ul>
 *   <li><strong>Domain → Entity:</strong> All relevant fields from the Persona domain object are explicitly mapped to the PersonaJpaEntity.</li>
 *   <li><strong>Entity → Domain:</strong> The PersonaJpaEntity is mapped back to a Persona domain object. JPA-specific fields like timestamps are ignored in the domain model.</li>
 *   <li><strong>Traits:</strong> The Map&lt;String, String&gt; for traits is directly mapped, relying on the database's JSONB support.</li>
 * </ul>
 *
 * @author Christian Langner
 * @version 1.1
 * @since 2026
 *
 * @see PersonaRepositoryPort
 * @see Persona
 * @see PersonaJpaEntity
 */
@Component
@Profile("home")
@RequiredArgsConstructor
@Slf4j
public class PersonaPersistenceAdapter implements PersonaRepositoryPort {

    private final SpringDataPersonaRepository personaRepository;

    @Override
    public Optional<Persona> findActiveByCompanyId(UUID companyId) {
        log.debug("Finding active persona for company: {}", companyId);
        return personaRepository.findByCompanyId(companyId) // Ensures only one is picked
                .map(this::mapToDomain);
    }

    @Override
    public Persona save(Persona persona) {
        log.info("Saving persona: id={}, companyId={}, type={}",
                persona.getId(), persona.getCompanyId(), persona.getPersonaType());
        PersonaJpaEntity entity = mapToEntity(persona);
        PersonaJpaEntity savedEntity = personaRepository.save(entity);
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
    public void deleteById(UUID id) {
        log.info("Deleting persona: id={}", id);
        if (personaRepository.existsById(id)) {
            personaRepository.deleteById(id);
            log.debug("Persona deleted successfully: id={}", id);
        } else {
            log.warn("Persona not found for deletion: id={}", id);
        }
    }

    private PersonaJpaEntity mapToEntity(Persona persona) {
        return PersonaJpaEntity.builder()
                .id(persona.getId())
                .companyId(persona.getCompanyId())
                .name(persona.getName())
                .personaType(persona.getPersonaType().name()) // Convert enum to String
                .role(persona.getRole()) // Added this line
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

    private Persona mapToDomain(PersonaJpaEntity entity) {
        return new Persona(
                entity.getId(),
                entity.getCompanyId(),
                entity.getName(),
                entity.getSystemPrompt(), // Corrected mapping
                entity.getSpeakingStyle(),
                entity.getTraits(),
                entity.getPromptTemplate(),
                entity.getRole(),
                entity.getExampleDialog(),
                entity.getVisualDna(),
                entity.getFixedSeed(),
                entity.getPersonaType() != null ? PersonaType.valueOf(entity.getPersonaType()) : PersonaType.SUPPORT,
                entity.getAllowExplicitContent() != null ? entity.getAllowExplicitContent() : false
        );
    }
}
