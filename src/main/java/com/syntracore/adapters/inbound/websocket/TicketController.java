// UPDATE #44: Bereinigter WebSocket TicketController
// Zweck: Synchronisation mit TicketService und Behebung von Syntaxfehlern
// Ort: src/main/java/com/syntracore/adapters/inbound/websocket/TicketController.java

package com.syntracore.adapters.inbound.websocket;

import com.syntracore.core.domain.SupportTicket;
import com.syntracore.core.services.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.UUID;

/**
 * WebSocket Controller für Echtzeit-Ticket-Interaktionen.
 * <p>
 * Dieser Inbound-Adapter ermöglicht die bidirektionale Kommunikation via STOMP.
 * Er ist direkt mit dem TicketService verdrahtet, um Use-Cases in Echtzeit abzubilden.
 * </p>
 * * @author Christian Langner
 * @version 2.2
 * @since 2026
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Verarbeitet eingehende Ticket-Erstellungsnachrichten über WebSocket.
     * Nutzt den RAG-Workflow des TicketService.
     *
     * @param customerName Name des Kunden
     * @param messageText Inhalt der Support-Anfrage
     */
    @MessageMapping("/tickets/create")
    public void createTicket(@Payload String customerName, @Payload String messageText) {
        log.info("WebSocket Ticket-Anfrage von: {}", customerName);

        // Nutzt die existierende Methode aus TicketService.java
        ticketService.createAndProcessTicket(customerName, messageText);

        // Broadcast einer Statusmeldung an alle Abonnenten
        messagingTemplate.convertAndSend("/topic/ticket-updates", "Neues Ticket für " + customerName + " wurde erstellt.");
    }

    /**
     * Markiert ein bestehendes Ticket über WebSocket als gelöst.
     *
     * @param ticketId Die UUID des Tickets als String
     */
    @MessageMapping("/tickets/resolve")
    public void resolveTicket(@Payload String ticketId) {
        UUID id = UUID.fromString(ticketId);
        log.info("Löse Ticket via WebSocket: {}", id);

        // Nutzt die existierende Methode aus TicketService.java
        ticketService.resolveTicket(id);

        messagingTemplate.convertAndSend("/topic/ticket-updates", "Ticket " + id + " wurde erfolgreich gelöst.");
    }

    /* * Hinweis: Die Methode 'sendAgentResponse' wurde vorerst entfernt/auskommentiert,
     * da der TicketService aktuell noch keine Methode 'addResponseToTicket' besitzt.
     * Dies verhindert Kompilierungsfehler im 'Kilo Code'.
     */
}