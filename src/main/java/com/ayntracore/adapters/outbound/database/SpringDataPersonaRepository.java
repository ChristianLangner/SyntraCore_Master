// Autor: Christian Langner
package com.ayntracore.adapters.outbound.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository für Personas.
 *
 * <p><strong>Architektur-Schicht:</strong> Infrastructure Layer (Data Access)</p>
 * <p><strong>Zweck:</strong> Automatische CRUD-Operationen für Persona-Tabelle</p>
 *
 * <h2>Spring Data JPA Features:</h2>
 * <ul>
 *   <li><strong>Method Naming:</strong> Automatische Query-Generierung</li>
 *   <li><strong>Type Safety:</strong> Compile-time Validierung</li>
 *   <li><strong>Transaction Management:</strong> Automatische Transaktionssteuerung</li>
 * </ul>
 *
 * @author Christian Langner
 * @version 1.0
 * @since 2026
 *
 * @see PersonaJpaEntity
 */
@Repository
public interface SpringDataPersonaRepository extends JpaRepository<PersonaJpaEntity, UUID> {

    /**
     * Findet die aktive Persona für eine Company.
     * Spring Data generiert automatisch die Query:
     * SELECT * FROM persona WHERE company_id = ?
     *
     * @param companyId Die Company-ID
     * @return Optional mit Persona, falls vorhanden
     */
    Optional<PersonaJpaEntity> findByCompanyId(UUID companyId);

    /**
     * Prüft, ob eine Persona für eine Company existiert.
     *
     * @param companyId Die Company-ID
     * @return true, wenn Persona existiert
     */
    boolean existsByCompanyId(UUID companyId);
}
