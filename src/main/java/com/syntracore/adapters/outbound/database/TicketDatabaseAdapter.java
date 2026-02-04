package com.syntracore.adapters.outbound.database;

import com.syntracore.core.domain.SupportTicket;
import com.syntracore.core.ports.TicketRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketDatabaseAdapter implements TicketRepositoryPort {

    private final SpringDataTicketRepository repository;

    @Override
    public void save(SupportTicket ticket) {
        TicketJpaEntity entity = new TicketJpaEntity();
        entity.setId(ticket.getId());
        entity.setCustomerName(ticket.getCustomerName());
        entity.setMessage(ticket.getMessage());
        entity.setCreatedAt(ticket.getCreatedAt());

        // Wichtig: KI-Analyse mit in die Entity mappen
        entity.setAiAnalysis(ticket.getAiAnalysis());

        repository.save(entity);
        System.out.println("💾 Ticket erfolgreich in der DB gespeichert: " + ticket.getId());
    }
}