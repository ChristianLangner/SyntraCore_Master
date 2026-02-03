package com.syntracore;

import com.syntracore.core.domain.SupportTicket;
import com.syntracore.core.ports.TicketRepositoryPort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Hauptklasse der SyntraCore-Anwendung.
 *
 * Diese Klasse ist der Einstiegspunkt der Spring-Boot-Anwendung und startet:
 * - Den Spring-Application-Context
 * - Den eingebetteten Webserver (z.B. Tomcat)
 * - Alle konfigurierten Beans und Komponenten
 */
@SpringBootApplication
public class SyntraCoreApplication {

    /**
     * Hauptmethode: Startet die Spring-Boot-Anwendung.
     *
     * @param args Kommandozeilenargumente (werden an Spring weitergegeben)
     */
    public static void main(String[] args) {
        SpringApplication.run(SyntraCoreApplication.class, args);
    }

    /**
     * CommandLineRunner Bean: Wird automatisch nach dem Start der Anwendung ausgeführt.
     *
     * Dieser "Herzschlag-Test" dient dazu:
     * - Die Verbindung zwischen Domain-Layer und Datenbank-Adapter zu testen
     * - Zu prüfen, ob die Datenbank korrekt konfiguriert ist
     * - Ein Beispiel-Ticket zu erstellen und zu speichern
     *
     * @param ticketPort Das Repository-Interface (wird automatisch von Spring injiziert)
     * @return CommandLineRunner, der beim Start ausgeführt wird
     */
    @Bean
    public CommandLineRunner heartbeatTest(TicketRepositoryPort ticketPort) {
        return args -> {
            System.out.println("💓 SyntraCore Herzschlag-Test startet...");

            // Schritt 1: Erstelle ein Domain-Objekt (reine Business-Logik, keine DB-Abhängigkeit)
            SupportTicket testTicket = new SupportTicket("Junior Admin", "System-Check: Läuft die DB?");

            // Schritt 2: Speichere über das Port-Interface (hexagonale Architektur)
            // Spring injiziert automatisch die konkrete Implementierung (TicketDatabaseAdapter)
            ticketPort.save(testTicket);

            System.out.println("✅ Herzschlag-Test erfolgreich! System ist bereit.");
        };
    }
}