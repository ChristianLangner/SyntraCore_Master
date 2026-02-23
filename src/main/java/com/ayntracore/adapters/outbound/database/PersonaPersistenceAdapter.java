package com.ayntracore.adapters.outbound.database;

import com.ayntracore.core.domain.Persona;
import com.ayntracore.core.domain.PersonaType;
import com.ayntracore.core.ports.PersonaRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonaPersistenceAdapter implements PersonaRepositoryPort {

    private final SpringDataPersonaRepository personaRepository;

    @Override
    public Persona save(Persona persona) {
        return mapToDomain(personaRepository.save(mapToEntity(persona)));
    }

    @Override
    public Optional<Persona> findById(UUID id) {
        return personaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public void deleteById(UUID id) {
        personaRepository.deleteById(id);
    }

    @Override
    public Optional<Persona> findActiveByCompanyId(UUID companyId) {
        return personaRepository.findByCompanyId(companyId).map(this::mapToDomain);
    }

    private Persona mapToDomain(PersonaJpaEntity entity) {
        return Persona.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .name(entity.getName())
                .role(entity.getRole())
                .personaType(safeMapPersonaType(entity.getPersonaType()))
                .active(entity.isActive())
                .allowExplicitContent(entity.getAllowExplicitContent())
                .systemPrompt(entity.getSystemPrompt())
                .speakingStyle(entity.getSpeakingStyle())
                .traits(entity.getTraits())
                .promptTemplate(entity.getPromptTemplate())
                .exampleDialog(entity.getExampleDialog())
                .visualDna(entity.getVisualDna())
                .fixedSeed(entity.getFixedSeed())
                .build();
    }

    private PersonaJpaEntity mapToEntity(Persona persona) {
        return PersonaJpaEntity.builder()
                .id(persona.getId())
                .companyId(persona.getCompanyId())
                .name(persona.getName())
                .personaType(persona.getPersonaType().name())
                .role(persona.getRole())
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

    private PersonaType safeMapPersonaType(String typeString) {
        if (typeString == null || typeString.isBlank()) return PersonaType.SUPPORT;
        try {
            return PersonaType.valueOf(typeString.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Mapping-Warning: Invalid PersonaType '{}'. Defaulting to SUPPORT.", typeString);
            return PersonaType.SUPPORT;
        }
    }
}