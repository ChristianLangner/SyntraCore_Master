// Autor: Christian Langner
package com.ayntracore.adapters.outbound.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository für Support-Tickets.
 * <p>
 * Implementiert automatische CRUD-Operationen auf der support_tickets Tabelle.
 * Bietet method naming conventions für spezielle Abfragemuster.
 * </p>
 * 
 * <p><strong>Architektur-Schicht:</strong> Outbound-Adapterschicht (Data Access Layer)</p>
 * <p><strong>Zweck:</strong> Automatische Bereitstellung von Ticket-Datenzugriffsmethoden mittels Spring Data JPA</p>
 * 
 * <h2>CRUD-Operationen:</h2>
 * <p>Automatsich durch JpaRepository bereitgestellt:</p>
 * <ul>
 *   <li><strong>Create:</strong> save(), saveAll()</li>
 *   <li><strong>Read:</strong> findById(), findAll(), findAllById()</li>
 *   <li><strong>Update:</strong> save() (optimistic locking unterstützt)</li>
 *   <li><strong>Delete:</strong> delete(), deleteAll(), deleteById()</li>
 * </ul>
 * 
 * <h2>Query Method Patterns:</h2>
 * <p>Spezielle Suchmethoden mittels Naming Convention:</p>
 * <ul>
 *   <li><strong>Statussuchen:</strong> findByStatus(), findByStatusIn()</li>
 *   <li><strong>Prioritätssuchen:</strong> findByPriority()</li>
 *   <li><strong>Kundenbezogene:</strong> findByCustomerId(), findByCustomerIdAndStatus()</li>
 *   <li><strong>Zeitbezogene:</strong> findByCreatedAtBefore(), findByCreatedAtAfter()</li>
 * </ul>
 * 
 * <h2>UUID-Vorteile:</h2>
 * <p>Sicheres ID-Management durch UUID-Primärschlüssel:</p>
 * <ul>
 *   <li><strong>Skalierbarkeit:</strong> Keine Konflikte in verteilten Datenbanken</li>
 *   <li><strong>Sicherheit:</strong> Nicht vorhersagbar wie sequentielle IDs</li>
 *   <li><strong>Migrierbarkeit:</strong> Datenbankunabhängige ID-Struktur</li>
 * </ul>
 * 
 * @author Christian Langner
 * @version 2.0
 * @since 2026
 * 
 * @see com.ayntracore.adapters.outbound.database.TicketJpaEntity
 * @see com.ayntracore.adapters.outbound.database.TicketDatabaseAdapter
 * @see org.springframework.data.repository.PagingAndSortingRepository
 */
@Repository
public interface SpringDataTicketRepository extends JpaRepository<TicketJpaEntity, UUID> {
    
    /**
     * Findet alle Tickets für eine bestimmte Company (Mandantenfähigkeit).
     * 
     * @param companyId Die UUID der Company
     * @return Liste aller Tickets für diese Company
     */
    List<TicketJpaEntity> findByCompanyId(UUID companyId);
    
    /**
     * Findet ein einzelnes Ticket für eine bestimmte Company (Mandantenfähigkeit).
     * Verhindert Cross-Tenant-Datenzugriff.
     * 
     * @param id Die UUID des Tickets
     * @param companyId Die UUID der Company
     * @return Optional mit dem Ticket, falls es existiert und zur Company gehört
     */
    Optional<TicketJpaEntity> findByIdAndCompanyId(UUID id, UUID companyId);
}
