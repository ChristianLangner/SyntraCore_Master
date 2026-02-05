package com.syntracore.adapters.inbound.web;

import com.syntracore.core.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST-Controller für Support-Ticket-Operationen (Inbound-Adapter).
 * 
 * <p>Diese Klasse ist ein <strong>Inbound-Adapter</strong> in der Hexagonalen Architektur
 * und bildet die <strong>Schnittstelle zwischen HTTP-Welt und Domain-Logik</strong>.</p>
 * 
 * <h2>Warum ist dieser Adapter wichtig?</h2>
 * <ul>
 *   <li><strong>Entkopplung:</strong> Die Domain-Schicht kennt kein HTTP, REST oder Spring.
 *       Dieser Adapter übersetzt HTTP-Requests in Domain-Operationen.</li>
 *   <li><strong>Austauschbarkeit:</strong> Die gleiche Domain-Logik könnte auch über einen
 *       Telegram-Bot, CLI oder GraphQL-API angesprochen werden - nur der Adapter ändert sich.</li>
 *   <li><strong>Verantwortlichkeit:</strong> Kümmert sich nur um HTTP-spezifische Dinge
 *       (Request-Parsing, Response-Formatierung), nicht um Business-Logik.</li>
 * </ul>
 * 
 * <h2>Architektur-Kontext:</h2>
 * <pre>
 * HTTP-Request → Inbound-Adapter (hier) → Service Layer → Domain Layer
 * 
 * Beispiel:
 * POST /api/tickets → TicketController → TicketService → SupportTicket
 * </pre>
 * 
 * <p><strong>Design-Prinzip:</strong> Der Controller ist "dünn" - er enthält keine
 * Business-Logik, sondern delegiert alles an den {@link com.syntracore.core.services.TicketService}.</p>
 * 
 * <h2>API-Endpunkte:</h2>
 * <ul>
 *   <li><code>POST /api/tickets</code> - Erstellt ein neues Support-Ticket</li>
 * </ul>
 * 
 * @author SyntraCore Development Team
 * @version 2.0
 * @since 2.0
 * 
 * @see com.syntracore.core.services.TicketService
 * @see com.syntracore.core.domain.SupportTicket
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    /**
     * Service-Instanz für Ticket-Operationen.
     * 
     * <p>Wird von Spring automatisch injiziert (Constructor Injection via Lombok's
     * {@code @RequiredArgsConstructor}). Dies ist die bevorzugte Injection-Methode,
     * da sie Testbarkeit und Immutability fördert.</p>
     */
    private final TicketService ticketService;

    /**
     * Erstellt ein neues Support-Ticket über einen HTTP POST-Request.
     * 
     * <p>Dieser Endpunkt nimmt Kundenname und Nachricht entgegen, erstellt ein Ticket,
     * führt eine KI-Analyse durch (mit RAG-Unterstützung) und speichert alles in der
     * Datenbank.</p>
     * 
     * <p><strong>HTTP-Beispiel:</strong></p>
     * <pre>
     * POST http://localhost:8080/api/tickets?name=Max%20Mustermann&message=Login%20Problem
     * 
     * Response:
     * "Ticket für Max Mustermann empfangen. Die KI-Analyse läuft im Hintergrund!"
     * </pre>
     * 
     * <p><strong>cURL-Beispiel:</strong></p>
     * <pre>
     * curl -X POST "http://localhost:8080/api/tickets?name=Max&message=Hilfe"
     * </pre>
     * 
     * <p><strong>Ablauf:</strong></p>
     * <ol>
     *   <li>Controller empfängt HTTP-Request</li>
     *   <li>Parameter werden extrahiert (name, message)</li>
     *   <li>Aufruf an {@link TicketService#createAndProcessTicket(String, String)}</li>
     *   <li>Service erstellt Ticket, ruft KI auf, speichert in DB</li>
     *   <li>Controller gibt Bestätigung zurück</li>
     * </ol>
     * 
     * <p><strong>Hinweis:</strong> Die KI-Analyse läuft synchron, d.h. der Request
     * wartet, bis die KI geantwortet hat. Für Produktionsumgebungen sollte dies
     * asynchron erfolgen.</p>
     * 
     * @param name Der Name des Kunden, der das Ticket erstellt (darf nicht leer sein)
     * @param message Die Problembeschreibung oder Anfrage des Kunden (darf nicht leer sein)
     * 
     * @return Eine Bestätigungsnachricht als String, die dem Kunden angezeigt wird
     * 
     * @throws org.springframework.web.bind.MissingServletRequestParameterException
     *         wenn name oder message fehlen
     */
    @PostMapping
    public String createTicket(@RequestParam String name, @RequestParam String message) {
        // Der Controller delegiert die gesamte Business-Logik an den Service.
        // Er kümmert sich nur um die HTTP-Schicht.
        ticketService.createAndProcessTicket(name, message);

        return "Ticket für " + name + " empfangen. Die KI-Analyse läuft im Hintergrund!";
    }
}