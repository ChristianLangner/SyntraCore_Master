package com.syntracore;

import com.syntracore.core.domain.SupportTicket;
import com.syntracore.core.ports.TicketRepositoryPort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SyntraCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyntraCoreApplication.class, args);
    }

    @Bean
    public CommandLineRunner heartbeatTest(TicketRepositoryPort ticketPort) {
        return args -> {
            System.out.println("💓 SyntraCore Herzschlag-Test startet...");

            // 1. Wir erstellen ein Ticket in der Domain-Welt
            SupportTicket testTicket = new SupportTicket("Junior Admin", "System-Check: Läuft die DB?");

            // 2. Wir schicken es durch den Port (das Interface)
            ticketPort.save(testTicket);

            System.out.println("✅ Herzschlag-Test erfolgreich! System ist bereit.");
        };
    }
}