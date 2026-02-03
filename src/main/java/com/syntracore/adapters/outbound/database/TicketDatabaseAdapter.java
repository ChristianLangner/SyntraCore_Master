package com.syntracore.adapters.outbound.database;

import com.syntracore.core.domain.SupportTicket;
import com.syntracore.core.ports.TicketRepositoryPort;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/**
 * Adapter-Klasse, die das Domain-Modell (`SupportTicket`) mit der Datenbank verbindet.
 *
 * Diese Klasse ist Teil der hexagonalen Architektur:
 * - Sie implementiert das `TicketRepositoryPort` Interface aus dem Domain-Layer
 * - Sie übersetzt Domain-Objekte (`SupportTicket`) in JPA-Entitäten (`TicketJpaEntity`)
 * - Sie nutzt das `SpringDataTicketRepository` für die tatsächliche Datenbankoperation
 *
 * Durch diese Trennung bleibt das Domain-Layer unabhängig von der Datenbank-Technologie.
 */
@Component // Spring erkennt diese Klasse als Bean und erstellt automatisch eine Instanz
@RequiredArgsConstructor // Lombok: Generiert automatisch einen Konstruktor für alle finalen Felder
public class TicketDatabaseAdapter implements TicketRepositoryPort {

    /**
     * Spring Data Repository für Datenbankoperationen.
     * Wird per Dependency Injection automatisch von Spring bereitgestellt.
     */
    private final SpringDataTicketRepository repository;

    /**
     * Speichert ein Domain-Ticket in der Datenbank.
     *
     * Diese Methode führt das Mapping zwischen Domain-Modell und Datenbank-Entität durch:
     * 1. Erstellt eine neue JPA-Entität
     * 2. Kopiert alle Daten vom Domain-Objekt zur Entität
     * 3. Speichert die Entität über das Repository in der Datenbank
     *
     * @param ticket Das Domain-Modell, das gespeichert werden soll
     */
    @Override
    public void save(SupportTicket ticket) {
        // Mapping-Schritt: Von Domain-Objekt zu JPA-Entität
        TicketJpaEntity entity = new TicketJpaEntity();
        entity.setId(ticket.getId());
        entity.setCustomerName(ticket.getCustomerName());
        entity.setMessage(ticket.getMessage());
        entity.setCreatedAt(ticket.getCreatedAt());

        // Persistierung: Speichern in der Datenbank über Spring Data JPA
        repository.save(entity);

        // Bestätigungsausgabe für Debugging/Monitoring
        System.out.println("💾 Ticket erfolgreich in der Datenbank gespeichert: " + ticket.getId());
    }
}