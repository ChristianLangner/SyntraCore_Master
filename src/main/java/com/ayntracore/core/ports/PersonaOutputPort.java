// Autor: Christian Langner
package com.ayntracore.core.ports;

import com.ayntracore.core.domain.Persona;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Port für Persona-Repository-Operationen.
 *
 * <p><strong>Architektur-Schicht:</strong> Port (Hexagonal Core)</p>
 * <p><strong>Framework-Unabhängigkeit:</strong> Keine Spring/JPA-Abhängigkeiten</p>
 *
 * <h2>Hexagonale Architektur:</h2>
 * <p>Dieser Port definiert die Schnittstelle, über die der Core mit der
 * Persistenz-Schicht kommuniziert. Die konkrete Implementierung erfolgt
 * durch einen Adapter (z.B. JPA-Adapter oder InMemory-Adapter).</p>
 *
 * <h2>Verantwortlichkeiten:</h2>
 * <ul>
 *   <li><strong>Laden:</strong> Persona für eine Company abrufen</li>
 *   <li><strong>Speichern:</strong> Persona persistieren oder aktualisieren</li>
 * </ul>
 *
 * <h2>Implementierungen:</h2>
 * <ul>
 *   <li><strong>DatabasePersonaAdapter:</strong> JPA-basierte Persistenz für Cloud-Deployment</li>
 *   <li><strong>InMemoryPersonaAdapter:</strong> In-Memory für lokale Entwicklung</li>
 * </ul>
 *
 * @author Christian Langner
 * @version 1.0
 * @since 2026
 *
 * @see Persona
 */
public interface PersonaOutputPort {

    /**
     * Lädt die aktive Persona für eine Company.
     *
     * @param companyId Eindeutige ID der Company
     * @return Optional mit Persona, falls vorhanden
     */
    Optional<Persona> findActiveByCompanyId(UUID companyId);

    /**
     * Speichert oder aktualisiert eine Persona.
     *
     * @param persona Die zu speichernde Persona
     * @return Die gespeicherte Persona mit aktualisierter ID
     */
    Persona save(Persona persona);

    /**
     * Lädt eine Persona anhand ihrer ID.
     *
     * @param id Eindeutige ID der Persona
     * @return Optional mit Persona, falls vorhanden
     */
    Optional<Persona> findById(UUID id);

    /**
     * Löscht eine Persona.
     *
     * @param id Eindeutige ID der zu löschenden Persona
     * @return true, wenn erfolgreich gelöscht
     */
    boolean deleteById(UUID id);
}
