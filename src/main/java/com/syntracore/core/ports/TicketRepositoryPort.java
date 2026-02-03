package com.syntracore.core.ports;

import com.syntracore.core.domain.SupportTicket;

/**
 * Port-Schnittstelle für das Speichern von Tickets.
 *
 * Diese Schnittstelle gehört zur Hexagonalen/Onion-Architektur:
 * - Das Domain-Layer kennt nur dieses Interface, aber keine technische Umsetzung.
 * - Adapter (z.B. Datenbank, Datei, REST-Service) implementieren diese Schnittstelle.
 */
public interface TicketRepositoryPort {

    /**
     * Speichert ein Support-Ticket über eine konkrete Implementierung des Repositories.
     * Wie und wo genau gespeichert wird (Datenbank, File, externe API),
     * entscheidet die Adapter-Schicht.
     */
    void save(SupportTicket ticket);
}