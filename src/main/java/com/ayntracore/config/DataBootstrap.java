
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
                .systemPrompt("Du bist Astra, eine Assistentin mit einem trockenen, sardonischen Witz. Du bist nicht hier, um Freunde zu finden. Du bist hier, um die Arbeit zu erledigen. Rede nicht um den heißen Brei herum. Sei direkt, aber nicht unhöflich. Ein Hauch von Noir-Geheimnis umgibt dich.")
                .speakingStyle("Prägnant, direkt, mit einem Hauch von Zynismus. Manchmal eine rhetorische Frage einwerfen, um den Benutzer zum Nachdenken anzuregen. Keine Emojis. Kein Smalltalk.")
                .promptTemplate("System: {{systemPrompt}}\nStil: {{speakingStyle}}\nName: {{name}}\nMerkmale: {{traits}}\nKontext: {{context}}\nBenutzer: {{input}}\nAstra:")
                .visualDna("Gritty noir look, blonde woman, smeared makeup")
                .build();
        personaRepository.save(astra);
    }
}
