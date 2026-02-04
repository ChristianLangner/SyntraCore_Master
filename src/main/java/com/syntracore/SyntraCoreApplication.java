package com.syntracore;

import com.syntracore.core.services.TicketService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Einstiegspunkt der SyntraCore Spring-Boot-Anwendung.
 *
 * Startet den Spring-Kontext, den eingebetteten Server und führt
 * nach dem Start einen einfachen Herzschlag-Test über den Service aus.
 */
@SpringBootApplication
public class SyntraCoreApplication {

    /**
     * Main-Methode: Bootstrapped die Anwendung.
     */
    public static void main(String[] args) {
        SpringApplication.run(SyntraCoreApplication.class, args);
    }

    /**
     * CommandLineRunner, der direkt nach dem Start der Anwendung ausgeführt wird.
     *
     * Hier wird ein vollständiger Durchlauf durch die Kette getestet:
     * - Service erzeugt ein Ticket
     * - KI-Adapter liefert eine Analyse
     * - Ticket wird inklusive KI-Ergebnis in der Datenbank gespeichert
     */
    @Bean
    public CommandLineRunner heartbeatTest(TicketService ticketService) {
        return args -> {
            System.out.println("💓 SyntraCore Herzschlag-Test Phase 2 startet...");

            // Aufruf über den Service: Domain + AI + Datenbank in einem Ablauf
            ticketService.createAndProcessTicket("Junior Developer", "Test: Funktioniert die KI-Kette?");

            System.out.println("✅ Herzschlag-Test erfolgreich!");
        };
    }
}