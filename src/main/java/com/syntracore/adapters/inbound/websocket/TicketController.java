// UPDATE #4: Revidierter TicketController
// Zweck: Korrekte Anbindung an das TicketUseCase-Interface und Behebung von Fehlern
// Ort: src/main/java/com/syntracore/adapters/inbound/web/TicketController.java

package com.syntracore.adapters.inbound.websocket;

import com.syntracore.core.ports.TicketUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST-Controller für Support-Ticket-Operationen (Inbound-Adapter).
 * * Diese Klasse bildet die Schnittstelle zwischen der HTTP-Außenwelt und der Domain-Logik.
 * Sie nutzt das TicketUseCase-Interface (Inbound Port), um lose Kopplung zu gewährleisten.
 * * @author SyntraCore Development Team
 * @version 3.1
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    /**
     * Inbound Port zur Steuerung der Geschäftslogik.
     * Nutzt das Interface statt der konkreten TicketService-Klasse.
     */
    private final TicketUseCase ticketUseCase;

    /**
     * Erstellt ein neues Support-Ticket über HTTP POST.
     * * @param name Der Name des Kunden
     * @param message Die Problembeschreibung
     * @return Bestätigungsmeldung
     */
    @PostMapping
    public String createTicket(@RequestParam String name, @RequestParam String message) {
        // Wir delegieren den Aufruf an den Port
        ticketUseCase.createAndProcessTicket(name, message);

        return "Ticket für " + name + " empfangen. Die KI-Analyse läuft im Hintergrund!";
    }

    /**
     * Test-Endpunkt für direkte KI-Anfragen ohne Ticket-Speicherung.
     * Hilfreich, um den RAG-Workflow schnell zu prüfen.
     * * @param message Die Benutzeranfrage
     * @return Die Antwort der KI
     */
    @GetMapping("/ask")
    public String askQuickQuestion(@RequestParam String message) {
        // Nutzt die neue Chat-Verarbeitung im Core
        return ticketUseCase.processInquiry(message);
    }
}