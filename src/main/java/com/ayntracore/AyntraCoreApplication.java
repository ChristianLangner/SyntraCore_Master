package com.ayntracore;

import com.ayntracore.core.services.TicketService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

/**
 * Hauptklasse und Einstiegspunkt der AyntraCore Spring-Boot-Anwendung.
 *
 * <p>Diese Klasse ist der <strong>Application Layer</strong> und koordiniert den Start
 * der gesamten Anwendung. Sie konfiguriert Spring Boot und führt einen automatischen
 * Herzschlag-Test beim Start durch.</p>
 *
 * <h2>Warum ist diese Klasse wichtig?</h2>
 * <ul>
 *   <li><strong>Application Bootstrap:</strong> Startet den Spring-Container und
 *       initialisiert alle Beans (Services, Adapter, Repositories).</li>
 *   <li><strong>Component Scanning:</strong> {@code @SpringBootApplication} aktiviert
 *       automatisches Scannen aller Komponenten im Package {@code com.ayntracore}.</li>
 *   <li><strong>Auto-Configuration:</strong> Spring Boot konfiguriert automatisch
 *       Datenbank, Web-Server, JPA, etc. basierend auf den Dependencies.</li>
 *   <li><strong>Smoke Test:</strong> Führt beim Start einen End-to-End-Test durch,
 *       um sicherzustellen, dass alle Komponenten korrekt verdrahtet sind.</li>
 * </ul>
 *
 * <h2>Was passiert beim Start?</h2>
 * <ol>
 *   <li><strong>Spring-Container wird initialisiert</strong>
 *       <ul>
 *         <li>Alle {@code @Component}, {@code @Service}, {@code @Repository} werden gescannt</li>
 *         <li>Dependency Injection wird aufgelöst</li>
 *         <li>Datenbank-Verbindung wird hergestellt (H2 In-Memory)</li>
 *       </ul>
 *   </li>
 *   <li><strong>Embedded Tomcat-Server startet</strong>
 *       <ul>
 *         <li>Läuft standardmäßig auf Port 8080</li>
 *         <li>REST-Endpunkte werden registriert</li>
 *       </ul>
 *   </li>
 *   <li><strong>Herzschlag-Test wird ausgeführt</strong>
 *       <ul>
 *         <li>Test-Ticket wird erstellt</li>
 *         <li>RAG-Workflow wird durchlaufen</li>
 *         <li>KI-Analyse wird generiert</li>
 *         <li>Ticket wird in Datenbank gespeichert</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * <h2>Architektur-Kontext:</h2>
 * <pre>
 * AyntraCoreApplication (hier)
 *   │
 *   ├─→ Spring Boot Auto-Configuration
 *   │   ├─→ Datenbank (H2)
 *   │   ├─→ Web-Server (Tomcat)
 *   │   └─→ JPA/Hibernate
 *   │
 *   ├─→ Component Scanning
 *   │   ├─→ Controllers (Inbound-Adapter)
 *   │   ├─→ Services (Use Cases)
 *   │   ├─→ Repositories (Outbound-Adapter)
 *   │   └─→ Domain-Modelle
 *   │
 *   └─→ Herzschlag-Test (CommandLineRunner)
 *       └─→ End-to-End-Test des RAG-Workflows
 * </pre>
 *
 * <h2>Konfiguration:</h2>
 * <p>Die Anwendung wird über {@code application.properties} konfiguriert:</p>
 * <ul>
 *   <li>Datenbank-Einstellungen (H2, PostgreSQL)</li>
 *   <li>KI-API-Konfiguration (OpenRouter)</li>
 *   <li>Server-Port und Logging</li>
 * </ul>
 *
 * @author Christian Langner
 * @version 3.0
 * @since 1.0
 *
 * @see com.ayntracore.core.services.TicketService
 * @see org.springframework.boot.SpringApplication
 */
@SpringBootApplication
public class AyntraCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(AyntraCoreApplication.class, args);
    }

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
}