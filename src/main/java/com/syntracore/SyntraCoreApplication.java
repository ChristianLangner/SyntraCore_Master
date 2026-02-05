package com.syntracore;

import com.syntracore.core.services.TicketService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Hauptklasse und Einstiegspunkt der SyntraCore Spring-Boot-Anwendung.
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
 *       automatisches Scannen aller Komponenten im Package {@code com.syntracore}.</li>
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
 * SyntraCoreApplication (hier)
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
 * @author SyntraCore Development Team
 * @version 3.0
 * @since 1.0
 * 
 * @see com.syntracore.core.services.TicketService
 * @see org.springframework.boot.SpringApplication
 */
@SpringBootApplication
public class SyntraCoreApplication {

    /**
     * Main-Methode: Einstiegspunkt der Java-Anwendung.
     * 
     * <p>Diese Methode wird von der JVM beim Start aufgerufen und bootstrapped
     * die gesamte Spring-Boot-Anwendung.</p>
     * 
     * <h3>Was passiert hier?</h3>
     * <ol>
     *   <li>Spring-Container wird initialisiert</li>
     *   <li>Auto-Configuration wird durchgeführt</li>
     *   <li>Component Scanning findet alle Beans</li>
     *   <li>Embedded Web-Server wird gestartet</li>
     *   <li>CommandLineRunner werden ausgeführt (Herzschlag-Test)</li>
     * </ol>
     * 
     * <p><strong>Verwendung:</strong></p>
     * <pre>
     * // In IDE: Rechtsklick → Run 'SyntraCoreApplication'
     * // Oder per Maven:
     * mvn spring-boot:run
     * 
     * // Oder als JAR:
     * java -jar syntracore.jar
     * </pre>
     * 
     * @param args Kommandozeilen-Argumente (werden an Spring weitergegeben)
     *             Beispiel: {@code --server.port=9090} ändert den Port
     */
    public static void main(String[] args) {
        SpringApplication.run(SyntraCoreApplication.class, args);
    }

    /**
     * Herzschlag-Test: Automatischer End-to-End-Test beim Anwendungsstart.
     * 
     * <p>Diese Methode definiert einen {@link CommandLineRunner}, der direkt nach
     * dem erfolgreichen Start der Anwendung ausgeführt wird. Er testet den kompletten
     * RAG-Workflow von Ticket-Erstellung bis Datenbank-Persistierung.</p>
     * 
     * <h3>Was wird getestet?</h3>
     * <ol>
     *   <li><strong>Service-Layer:</strong> {@link TicketService} ist korrekt verdrahtet</li>
     *   <li><strong>Domain-Layer:</strong> {@link com.syntracore.core.domain.SupportTicket} funktioniert</li>
     *   <li><strong>RAG-Workflow:</strong> Wissensdatenbank-Zugriff funktioniert</li>
     *   <li><strong>KI-Integration:</strong> OpenAI-Adapter kann KI-Antworten generieren</li>
     *   <li><strong>Datenbank:</strong> JPA-Persistierung funktioniert</li>
     * </ol>
     * 
     * <h3>Erwartete Konsolen-Ausgabe:</h3>
     * <pre>
     * 💓 SyntraCore Herzschlag-Test Phase 2 startet...
     * 🔍 Suche passendes Wissen für: Test: Funktioniert die KI-Kette?
     * 💾 Ticket erfolgreich in der DB gespeichert: [UUID]
     * 🚀 Service: Ticket verarbeitet (RAG aktiv).
     * ✅ Herzschlag-Test erfolgreich!
     * </pre>
     * 
     * <p><strong>Hinweis:</strong> Wenn dieser Test fehlschlägt, ist die Anwendung
     * nicht korrekt konfiguriert (z.B. fehlender API-Key, Datenbank-Problem).</p>
     * 
     * <p><strong>Best Practice:</strong> In Produktionsumgebungen sollte dieser Test
     * durch ein Profil deaktivierbar sein (z.B. {@code @Profile("!prod")}).</p>
     * 
     * @param ticketService Der {@link TicketService}, der von Spring injiziert wird
     * 
     * @return Ein {@link CommandLineRunner}, der den Test ausführt
     */
    @Bean
    public CommandLineRunner heartbeatTest(TicketService ticketService) {
        return args -> {
            System.out.println("💓 SyntraCore Herzschlag-Test Phase 2 startet...");

            // End-to-End-Test: Vollständiger Durchlauf durch alle Schichten
            // Domain → Service → Ports → Adapter (KI + DB)
            ticketService.createAndProcessTicket("Junior Developer", "Test: Funktioniert die KI-Kette?");

            System.out.println("✅ Herzschlag-Test erfolgreich!");
        };
    }
}