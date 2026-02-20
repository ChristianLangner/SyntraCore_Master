
package com.ayntracore.config;

import com.ayntracore.adapters.outbound.database.PersonaJpaEntity;
import com.ayntracore.adapters.outbound.database.SpringDataPersonaRepository;
import com.ayntracore.core.domain.PersonaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DataBootstrap implements ApplicationListener<ApplicationReadyEvent> {

    private final SpringDataPersonaRepository personaRepository;

    @Autowired
    public DataBootstrap(SpringDataPersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        UUID personaId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        if (!personaRepository.existsById(personaId)) {
            PersonaJpaEntity astra = PersonaJpaEntity.builder()
                    .id(personaId)
                    .companyId(personaId) // Using the same ID for companyId as per previous context
                    .name("Astra")
                    .role("Ayntra Core Agent")
                    .personaType(PersonaType.SUPPORT) // Corrected from ASSISTANT to SUPPORT
                    .allowExplicitContent(false)
                    .systemPrompt("You are Astra, a helpful assistant.")
                    .build();
            personaRepository.save(astra);
        }
    }
}
