// Autor: Christian Langner
package com.ayntracore.adapters.outbound.database;

import com.ayntracore.core.domain.Persona;
import com.ayntracore.core.ports.PersonaRepositoryPort;
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

    @Override
    public Optional<Persona> findById(UUID id) {
        return activeByCompanyId.values().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    @Override
    public void deleteById(UUID id) {
        activeByCompanyId.values().removeIf(p -> p.getId().equals(id));
    }
}
