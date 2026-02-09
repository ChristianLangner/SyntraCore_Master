// Autor: Christian Langner
package com.ayntracore.adapters.outbound.database;

import com.ayntracore.core.domain.SupportTicket;
import com.ayntracore.core.ports.TicketRepositoryPort;
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
        entity.setResolved(ticket.isResolved());
        entity.setCompanyId(ticket.getCompanyId());

        repository.save(entity);
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
        UUID effectiveCompanyId = entity.getCompanyId() != null ? entity.getCompanyId() : UUID.randomUUID();
        
        SupportTicket ticket = new SupportTicket(
                entity.getCustomerName(),
                entity.getMessage(),
                effectiveCompanyId
        );
        ticket.setId(entity.getId());
        ticket.setCreatedAt(entity.getCreatedAt());
        ticket.setAiAnalysis(entity.getAiAnalysis());
        ticket.setResolved(entity.isResolved());
        return ticket;
    }
}
