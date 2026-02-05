// UPDATE #34: Vollständiger TicketDatabaseAdapter (UUID-Support)
// Ort: src/main/java/com/syntracore/adapters/outbound/database/TicketDatabaseAdapter.java

package com.syntracore.adapters.outbound.database;

import com.syntracore.core.domain.SupportTicket;
import com.syntracore.core.ports.TicketRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
        entity.setAiAnalysis(ticket.getAiAnalysis());
        // Falls resolved im Domain-Modell ist, hier auch mappen:
        // entity.setResolved(ticket.isResolved());

        repository.save(entity);
        System.out.println("💾 Ticket persistent gespeichert: " + ticket.getId());
    }

    @Override
    public List<SupportTicket> findAll() {
        return repository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SupportTicket> findById(UUID id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    private SupportTicket mapToDomain(TicketJpaEntity entity) {
        SupportTicket ticket = new SupportTicket(entity.getCustomerName(), entity.getMessage());
        ticket.setId(entity.getId());
        ticket.setCreatedAt(entity.getCreatedAt());
        ticket.setAiAnalysis(entity.getAiAnalysis());
        // ticket.setResolved(entity.isResolved());
        return ticket;
    }
}