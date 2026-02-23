package com.ayntracore.adapters.outbound.database;

import com.ayntracore.core.domain.Persona;
import com.ayntracore.core.ports.PersonaOutputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

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
    public boolean deleteById(UUID id) {
        log.info("Deleting persona: id={}", id);
        if (personaRepository.existsById(id)) {
            personaRepository.deleteById(id);
            log.debug("Persona deleted successfully: id={}", id);
            return true;
        } else {
            log.warn("Persona not found for deletion: id={}", id);
            return false;
        }
    }

    private PersonaJpaEntity mapToEntity(Persona persona) {
        return PersonaJpaEntity.builder()
                .id(persona.getId())
                .companyId(persona.getCompanyId())
                .name(persona.getName())
                .role(persona.getRole())
                .personaType(persona.getPersonaType())
                .active(persona.isActive())
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
                entity.getRole(),
                entity.getPersonaType(),
                entity.isActive(),
                entity.getAllowExplicitContent(),
                entity.getSystemPrompt(),
                entity.getSpeakingStyle(),
                entity.getVisualDna(),
                entity.getFixedSeed(),
                entity.getTraits(),
                entity.getPromptTemplate(),
                entity.getExampleDialog()
        );
    }
}
