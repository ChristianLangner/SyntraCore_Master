// Autor: Christian Langner
package com.ayntracore;

import com.ayntracore.core.domain.Persona;
import com.ayntracore.core.ports.PersonaOutputPort;
// import com.ayntracore.core.services.TicketService; // Auskommentiert, da heartbeatTest deaktiviert ist
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Map;
import java.util.UUID;

@SpringBootApplication
public class AyntraCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(AyntraCoreApplication.class, args);
    }

    /*
    @Bean
    public CommandLineRunner heartbeatTest(
            TicketService ticketService,
            @Value("${ayntracore.heartbeat.enabled:false}") boolean heartbeatEnabled
    ) {
        return args -> {
            if (!heartbeatEnabled) {
                System.out.println("💓 AyntraCore Herzschlag-Test: deaktiviert (ayntracore.heartbeat.enabled=false)");
                return;
            }

            System.out.println("💓 AyntraCore Herzschlag-Test startet...");

            UUID testId = UUID.randomUUID();
            ticketService.createAndProcessTicket("Junior Developer", "Test: Funktioniert die KI-Kette?", testId);

            System.out.println("✅ Herzschlag-Test erfolgreich!");
        };
    }
    */

    @Bean
    public CommandLineRunner createTestPersona(PersonaOutputPort personaOutputPort) {
        return args -> {
            UUID companyId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            if (personaOutputPort.findActiveByCompanyId(companyId).isEmpty()) {
                Persona testPersona = new Persona();
                testPersona.setCompanyId(companyId);
                testPersona.setName("Ayntra Wissensbot");
                testPersona.setTraits(Map.of(
                        "primaryColor", "#0EA5E9",
                        "theme", "dark",
                        "referenceImageUrl", "https://i.imgur.com/8NTRb12.png",
                        "modelId", "RealVisXL_V4.0",
                        "denoiseStrength", "0.75"
                ));
                testPersona.setActive(true);
                personaOutputPort.save(testPersona);
                System.out.println("[BOOTSTRAP] Test-Persona created for companyId: " + companyId);
            }
        };
    }
}
