package com.syntracore.adapters.outbound.database;

import com.syntracore.core.domain.Persona;
import com.syntracore.core.ports.PersonaRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Outbound Adapter: einfache In-Memory Persona Ablage pro Company.
 * Später ersetzbar durch JPA/DB Adapter.
 */
@Component
public class InMemoryPersonaAdapter implements PersonaRepositoryPort {

    private final Map<UUID, Persona> activeByCompanyId = new ConcurrentHashMap<>();

    @Override
    public Optional<Persona> findActiveByCompanyId(UUID companyId) {
        return Optional.ofNullable(activeByCompanyId.get(companyId));
    }

    @Override
    public Persona save(Persona persona) {
        activeByCompanyId.put(persona.getCompanyId(), persona);
        return persona;
    }
}
