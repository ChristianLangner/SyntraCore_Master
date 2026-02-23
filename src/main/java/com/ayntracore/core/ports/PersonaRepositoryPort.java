// Autor: Christian Langner
package com.ayntracore.core.ports;

import com.ayntracore.core.domain.Persona;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Port für Persona-Konfiguration (Bot-Charakter) pro Company.
 */
public interface PersonaRepositoryPort {

    Optional<Persona> findActiveByCompanyId(UUID companyId);

    Persona save(Persona persona);
    
    Optional<Persona> findById(UUID id);

    void deleteById(UUID id);
}
