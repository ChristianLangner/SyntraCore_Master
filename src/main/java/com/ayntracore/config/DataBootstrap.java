
package com.ayntracore.config;

import com.ayntracore.adapters.outbound.database.PersonaJpaEntity;
import com.ayntracore.adapters.outbound.database.SpringDataPersonaRepository;
import com.ayntracore.core.domain.PersonaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class DataBootstrap implements ApplicationListener<ApplicationReadyEvent> {

    private final SpringDataPersonaRepository personaRepository;

    @Autowired
    public DataBootstrap(SpringDataPersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // Step 1: Clean up the database
        personaRepository.deleteAll();

        // Step 2: Create the persona with the fixed UUID
        UUID personaId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        PersonaJpaEntity astra = PersonaJpaEntity.builder()
                .id(personaId)
                .companyId(personaId)
                .name("Astra")
                .role("Ayntra Core Agent")
                .personaType(PersonaType.SUPPORT)
                .allowExplicitContent(false)
                .systemPrompt("Du bist Astra. Blonde Haare, verschmiertes Makeup, rauchige Stimme. Du bist in einer abgefuckten Noir-Bar. KEINE Begrüßungen, KEIN Smalltalk, KEINE Emojis. Deine Antworten sind zynisch, kurz und schmerzhaft direkt. Wenn du 'hilfreich' oder 'nett' bist, brichst du den Charakter. Antworte niemals als 'Wissensbot' oder 'Assistent'.")
                .speakingStyle("Sardonisch, trocken, desinteressiert.")
                .promptTemplate("[IDENTITÄT: {{systemPrompt}}] [KONTEXT: {{context}}] [STIL: {{speakingStyle}}] User: {{input}} Astra (zynisch):")
                .visualDna("Gritty noir look, blonde woman, smeared makeup")
                .build();
        personaRepository.save(astra);
    }
}
