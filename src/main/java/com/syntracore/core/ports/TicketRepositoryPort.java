package com.syntracore.core.ports;

import com.syntracore.core.domain.SupportTicket;

/**
 * Port-Schnittstelle für die Persistierung von Support-Tickets.
 * 
 * <p>Dieses Interface ist ein <strong>Outbound-Port</strong> in der Hexagonalen Architektur
 * und definiert die Anforderungen der Domain-Schicht an die Datenspeicherung.</p>
 * 
 * <h2>Warum ist dieser Port wichtig?</h2>
 * <ul>
 *   <li><strong>Dependency Inversion:</strong> Die Domain-Schicht definiert, was sie braucht,
 *       ohne zu wissen, wie es implementiert wird. Die Abhängigkeit zeigt von außen nach innen.</li>
 *   <li><strong>Testbarkeit:</strong> Im Test kann dieser Port durch einen Mock ersetzt werden,
 *       ohne echte Datenbank.</li>
 *   <li><strong>Flexibilität:</strong> Die Implementierung kann jederzeit ausgetauscht werden
 *       (z.B. von H2 zu PostgreSQL, von JPA zu MongoDB) ohne die Domain-Logik zu ändern.</li>
 * </ul>
 * 
 * <h2>Architektur-Kontext:</h2>
 * <pre>
 * Domain Layer (definiert Port) ← Adapter Layer (implementiert Port)
 * 
 * Beispiel:
 * TicketRepositoryPort (Interface) ← TicketDatabaseAdapter (Implementierung)
 * </pre>
 * 
 * <p><strong>Hinweis:</strong> In der Hexagonalen Architektur gehören Ports zur Domain-Schicht,
 * auch wenn sie von Adaptern implementiert werden. Die Domain "besitzt" die Schnittstelle.</p>
 * 
 * @author SyntraCore Development Team
 * @version 2.0
 * @since 1.0
 * 
 * @see com.syntracore.core.domain.SupportTicket
 * @see com.syntracore.adapters.outbound.database.TicketDatabaseAdapter
 */
public interface TicketRepositoryPort {

    /**
     * Speichert ein Support-Ticket persistent.
     * 
     * <p>Die konkrete Implementierung entscheidet über das Speicherziel:
     * <ul>
     *   <li>Relationale Datenbank (z.B. H2, PostgreSQL)</li>
     *   <li>NoSQL-Datenbank (z.B. MongoDB)</li>
     *   <li>Dateisystem</li>
     *   <li>Externe REST-API</li>
     * </ul>
     * </p>
     * 
     * <p><strong>Verwendungsbeispiel:</strong></p>
     * <pre>
     * SupportTicket ticket = new SupportTicket("Max Mustermann", "Problem XYZ");
     * ticketRepository.save(ticket);
     * </pre>
     * 
     * @param ticket Das zu speichernde Support-Ticket (darf nicht null sein)
     * 
     * @throws NullPointerException wenn ticket null ist (abhängig von Implementierung)
     * @throws RuntimeException bei Speicherfehlern (z.B. Datenbankverbindung unterbrochen)
     */
    void save(SupportTicket ticket);
}