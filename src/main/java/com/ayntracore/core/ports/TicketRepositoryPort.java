// Autor: Christian Langner
package com.ayntracore.core.ports;

import com.ayntracore.core.domain.SupportTicket;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Port – Driven Port für Ticket-Datenbankoperationen.
 * <p>
 * Definiert die Schnittstelle für persistente Ticket-Speicherung und -Abfragen.
 * Verwendet UUIDs als Primärschlüssel zur Gewährleistung globaler Eindeutigkeit
 * und Cloud-Kompatibilität in verteilten Systemen.
 * </p>
 * 
 * @see Outbound-Port gemäß hexagonaler Architektur
 * @author Christian Langner
 * @version 2.0
 * @since 2026
 */
public interface TicketRepositoryPort {

    /**
     * Persistiert ein SupportTicket in der Datenbank.
     * Die UUID-Identifikation gewährleistet Thread-Safety und Clustering.
     *
     * @param ticket Das zu speichernde SupportTicket
     */
    void save(SupportTicket ticket);

    /**
     * Liefert alle vorhandenen SupportTickets aus der Datenbank.
     * DEPRECATED: Verwende stattdessen {@link #findAllByCompanyId(UUID)} für Mandantenfähigkeit.
     *
     * @return Liste aller SupportTickets, leer falls keine vorhanden
     */
    @Deprecated
    List<SupportTicket> findAll();

    /**
     * Sucht nach einem SupportTicket anhand seiner eindeutigen UUID.
     *
     * @param id Die UUID des gesuchten Tickets
     * @return Optional mit dem gefundenen Ticket oder empty
     */
    Optional<SupportTicket> findById(UUID id);

    /**
     * Sucht nach allen SupportTickets für eine bestimmte Company (Mandant).
     * Unterstützt Multi-Tenancy durch Filterung nach companyId.
     *
     * @param companyId Die UUID der Company
     * @return Liste aller Tickets für diese Company, leer falls keine vorhanden
     */
    List<SupportTicket> findAllByCompanyId(UUID companyId);

    /**
     * Sucht nach einem SupportTicket für eine bestimmte Company.
     * Validiert, dass das Ticket zur angeforderten Company gehört.
     *
     * @param id Die UUID des Tickets
     * @param companyId Die UUID der Company
     * @return Optional mit dem gefundenen Ticket oder empty
     */
    Optional<SupportTicket> findByIdAndCompanyId(UUID id, UUID companyId);
}
