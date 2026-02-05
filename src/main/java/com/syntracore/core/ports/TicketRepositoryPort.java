// UPDATE #26: TicketRepositoryPort mit UUID
// Ort: src/main/java/com/syntracore/core/ports/TicketRepositoryPort.java

package com.syntracore.core.ports;

import com.syntracore.core.domain.SupportTicket;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepositoryPort {
    void save(SupportTicket ticket);
    List<SupportTicket> findAll();
    Optional<SupportTicket> findById(UUID id);
}