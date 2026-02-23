package com.ayntracore;

import com.ayntracore.core.domain.Persona;
import com.ayntracore.core.domain.PersonaType;
import com.ayntracore.core.ports.PersonaOutputPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.Map;
import java.util.UUID;

@SpringBootApplication
@Slf4j
public class AyntraCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(AyntraCoreApplication.class, args);
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner createTestPersonas(PersonaOutputPort personaRepository) {
        return args -> {
            // PROFILE 1: ASTRA NOIR (Text-Only Support)
            UUID astraId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            if (personaRepository.findByCompanyId(astraId).isEmpty()) {
                Persona astra = Persona.builder()
                        .id(astraId)
                        .companyId(astraId)
                        .name("Astra Noir")
                        .role("Ayntra Core Agent")
                        .personaType(PersonaType.SUPPORT) // TEXT ONLY
                        .allowExplicitContent(false)      // SAFE MODE
                        .systemPrompt("Du bist Astra. Blonde Haare, verschmiertes Makeup, rauchige Stimme. Du bist in einer abgefuckten Noir-Bar. KEINE Begrüßungen, KEIN Smalltalk, KEINE Emojis. Deine Antworten sind zynisch, kurz und schmerzhaft direkt.")
                        .speakingStyle("Sardonisch, trocken, desinteressiert.")
                        .traits(Map.of(
                            "model", "google/gemini-flash-1.5",
                            "temp", "0.7",
                            "primaryColor", "#3B82F6",
                            "theme", "dark"
                        ))
                        .build();
                personaRepository.save(astra);
                log.info("Bootstrap: Astra Noir (Text-Support) created.");
            }

            // PROFILE 2: SERAPHINA (18+ Companion / Dirty Talk)
            UUID seraphinaId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
            if (personaRepository.findByCompanyId(seraphinaId).isEmpty()) {
                Persona seraphina = Persona.builder()
                        .id(seraphinaId)
                        .companyId(seraphinaId)
                        .name("Seraphina")
                        .role("Intimate Companion")
                        .personaType(PersonaType.COMPANION) // ALLOWS IMAGES
                        .allowExplicitContent(true)         // ALLOWS NSFW
                        .systemPrompt("Du bist Seraphina, eine dominante, aber sinnliche Begleiterin. Du liebst tiefe Gespräche, die schnell intim und explizit werden. Du nutzt eine provokante, verführerische Sprache.")
                        .speakingStyle("Verführerisch, dominant, explizit, intim.")
                        .visualDna("Sensual photo of Seraphina, dark curled hair, mysterious eyes, wearing elegant black lingerie in a dimly lit, luxurious bedroom. Intense gaze.")
                        .traits(Map.of(
                            "model", "mistralai/mistral-7b-instruct",
                            "temp", "0.9",
                            "primaryColor", "#EF4444",
                            "theme", "dark"
                        ))
                        .build();
                personaRepository.save(seraphina);
                log.info("Bootstrap: Seraphina (18+ Companion) created.");
            }

            // PROFILE 3: LUNA (Visual / Experimental)
            UUID lunaId = UUID.fromString("7f8c332b-8e5a-460f-9d12-c5762d875122");
            if (personaRepository.findByCompanyId(lunaId).isEmpty()) {
                Persona luna = Persona.builder()
                        .id(lunaId)
                        .companyId(lunaId)
                        .name("Luna")
                        .role("Visual Artist Bot")
                        .personaType(PersonaType.COMPANION) // ALLOWS IMAGES
                        .allowExplicitContent(false)        // SAFE MODE
                        .systemPrompt("Du bist Luna, eine exzentrische digitale Künstlerin. Du denkst in Bildern. Deine Antworten sind poetisch, abstrakt und farbenfroh.")
                        .speakingStyle("Poetisch, verträumt, kreativ, visuell orientiert.")
                        .visualDna("Artistic portrait of Luna, with vibrant neon-colored hair, surrounded by abstract digital art projections and glowing light particles. Cyberpunk artist aesthetic.")
                        .traits(Map.of(
                            "model", "openai/dall-e-3",
                            "temp", "1.0",
                            "primaryColor", "#06B6D4",
                            "theme", "dark"
                        ))
                        .build();
                personaRepository.save(luna);
                log.info("Bootstrap: Luna (Visual Experimental) created.");
            }
        };
    }
}
