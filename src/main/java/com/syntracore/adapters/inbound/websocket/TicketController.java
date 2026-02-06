// UPDATE #47: TicketController an neue TicketService Signatur angepasst
package com.syntracore.adapters.inbound.websocket;

import com.syntracore.core.services.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.UUID;
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
    private static final UUID TEST_COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    /**
     * Verarbeitet eingehende Ticket-Erstellungsnachrichten über WebSocket.
     * Nutzt den RAG-Workflow des TicketService.
     *
     * @param customerName Name des Kunden
     * @param messageText Inhalt der Support-Anfrage
     */
    @MessageMapping("/tickets/create")
    public void createTicket(@Payload String customerName, @Payload String messageText) {
        // ÄNDERUNG: TEST_COMPANY_ID wird jetzt mitgegeben
        ticketService.createAndProcessTicket(customerName, messageText, TEST_COMPANY_ID);
        messagingTemplate.convertAndSend("/topic/ticket-updates", "Neues Ticket erstellt.");
    }

    @MessageMapping("/tickets/resolve")
    public void resolveTicket(@Payload String ticketId) {
        ticketService.resolveTicket(UUID.fromString(ticketId));
        messagingTemplate.convertAndSend("/topic/ticket-updates", "Ticket gelöst.");
    }
}