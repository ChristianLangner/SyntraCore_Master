package com.syntracore.adapters.outbound.database;

// JPA-Annotationen für die Abbildung auf die Datenbank
import jakarta.persistence.*;
// Zeitstempel für Erstellungsdatum
import java.time.LocalDateTime;
// Eindeutige ID für das Ticket
import java.util.UUID;
// Lombok: Generiert automatisch Getter- und Setter-Methoden zur Compile-Zeit
import lombok.Getter;
import lombok.Setter;

/**
 * Persistente Entität für ein Support-Ticket.
 * Diese Klasse repräsentiert die Datenbank-Tabelle `tickets`.
 * Sie wird im Adapter-Layer genutzt, um Domain-Objekte in Datenbankobjekte zu übersetzen.
 */
@Entity                             // Markiert diese Klasse als JPA-Entität
@Table(name = "tickets")           // Verknüpft die Entität mit der Tabelle "tickets"
@Getter                            // Lombok: erzeugt Getter für alle Felder
@Setter                            // Lombok: erzeugt Setter für alle Felder
public class TicketJpaEntity {

    /**
     * Primärschlüssel des Tickets.
     * UUID sorgt für weltweit eindeutige IDs ohne zentrale Datenbank-Sequenz.
     */
    @Id
    private UUID id;

    /**
     * Name des Kunden, der das Ticket erstellt hat.
     */
    private String customerName;

    /**
     * Nachricht bzw. Beschreibung des Problems/Anliegens.
     */
    private String message;

    /**
     * Zeitpunkt, zu dem das Ticket erstellt wurde.
     */
    private LocalDateTime createdAt;

    /**
     * Leerer Standard-Konstruktor.
     * Wird von JPA zwingend benötigt, um Objekte aus der Datenbank zu instanziieren.
     */
    public TicketJpaEntity() {}
}