package com.syntracore.adapters.outbound.database;

import com.syntracore.core.domain.SupportTicket;
import com.syntracore.core.ports.TicketRepositoryPort;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component // Damit Spring diesen Adapter erkennt
@RequiredArgsConstructor // Erzeugt automatisch den Konstruktor für das Repository
public class TicketDatabaseAdapter implements TicketRepositoryPort {

    private final SpringDataTicketRepository repository;

    @Override
    public void save(SupportTicket ticket) {
        // Hier passiert das Mapping: Von Domain zu JPA
        TicketJpaEntity entity = new TicketJpaEntity();
        entity.setId(ticket.getId());
        entity.setCustomerName(ticket.getCustomerName());
        entity.setMessage(ticket.getMessage());
        entity.setCreatedAt(ticket.getCreatedAt());

        repository.save(entity);
        System.out.println("💾 Ticket erfolgreich in der Datenbank gespeichert: " + ticket.getId());
    }
}