package com.syntracore.adapters.inbound.web;

import com.syntracore.core.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Inbound Adapter für Web-Anfragen.
 * Er nimmt HTTP-Requests entgegen und leitet sie an den Core weiter.
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * Erstellt ein neues Ticket über einen POST-Request.
     * Nutze dies für deinen lokalen Test.
     */
    @PostMapping
    public String createTicket(@RequestParam String name, @RequestParam String message) {
        // Der Controller delegiert die Arbeit an den Service
        ticketService.createAndProcessTicket(name, message);

        return "Ticket für " + name + " empfangen. Die KI-Analyse läuft im Hintergrund!";
    }
}