// UPDATE #47: TicketController an neue TicketService Signatur angepasst
package com.ayntracore.adapters.inbound.websocket;

import com.ayntracore.core.services.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

/**
 * WebSocket Controller für Echtzeit-Ticket-Interaktionen.
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final UUID TEST_COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @MessageMapping("/tickets/create")
    public void createTicket(@Payload String customerName, @Payload String messageText) {
        ticketService.createAndProcessTicket(customerName, messageText, TEST_COMPANY_ID);
        messagingTemplate.convertAndSend("/topic/ticket-updates", "Neues Ticket erstellt.");
    }

    @MessageMapping("/tickets/resolve")
    public void resolveTicket(@Payload String ticketId) {
        ticketService.resolveTicket(UUID.fromString(ticketId));
        messagingTemplate.convertAndSend("/topic/ticket-updates", "Ticket gelöst.");
    }
}