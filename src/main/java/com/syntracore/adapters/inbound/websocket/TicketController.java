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
 * Empfängt und verarbeitet Nachrichten über STOMP-WebSockets.
 * Ermöglicht bidirektionale Kommunikation zwischen Frontend und Backend.
 * </p>
 * 
 * <p><strong>Architektur-Schicht:</strong> Inbound-Adapter (WebSocket-Endpunkt)</p>
 * <p><strong>Zweck:</strong> Adapter für bidirektionale Echtzeitkommunikation via WebSockets</p>
 * 
 * <h2>WebSocket-Architektur:</h2>
 * <ul>
 *   <li><strong>STOMP Protocol:</strong> Message-oriented Protokoll über WebSockets</li>
 *   <li><strong>Topic-Basiert:</strong> Nachrichten werden an Topics gesendet</li>
 *   <li><strong>Single-User Topics:</strong> /user/queue/tickets für persönliche Updates</li>
 *   <li><strong>Broadcast Topics:</strong> /topic/updates für allgemeine Nachrichten</li>
 * </ul>
 * 
 * <h2>Message Flows:</h2>
 * <table border="1">
 *   <tr><th>Message Type</th><th>Queue/Topic</th><th>Beschreibung</th></tr>
 *   <tr><td>Ticket Creation</td><td>/app/tickets/create</td><td>Erstellt neues Support-Ticket</td></tr>
 *   <tr><td>Status Updates</td><td>/topic/ticket-updates</td><td>Broadcast Statusänderungen</td></tr>
 *   <tr><td>Agent Responses</td><td>/user/queue/tickets</td><td>Persönliche Agent-Antworten</td></tr>
 * </table>
 * 
 * <h2>Resilienz-Patterns:</h2>
 * <ul>
 *   <li><strong>Connection Lost Handling:</strong> WebSockets rekonnektieren automatisch</li>
 *   <li><strong>Message Delivery Assurance:</strong> STOMP garantiert Nachrichtenzustellung</li>
 *   <li><strong>Scalability:</strong> WebSocket-Sessions können über Cluster verteilt werden</li>
 * </ul>
 * 
 * @author Christian Langner
 * @version 2.0
 * @since 2026
 * 
 * @see com.syntracore.adapters.inbound.websocket.WebSocketConfig
 * @see org.springframework.messaging.simp.SimpMessagingTemplate
 * @see com.syntracore.core.services.TicketService
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Verarbeitet eingehende Ticket-Erstellungsnachrichten über WebSocket.
     *
     * @param customerId Identifier des anfragenden Kunden (UUID-Format)
     * @param messageText Problembeschreibung des Kunden
     * @return Erstelltes Support-Ticket als JSON-Response
     */
    @MessageMapping("/tickets/create")
    public SupportTicket createTicket(@Payload String customerId, @Payload String messageText) {
        UUID customerUuid = UUID.fromString(customerId);
        log.info("Receiving WebSocket ticket creation request for customer: {}", customerUuid);
        
        SupportTicket ticket = ticketService.createTicket(customerUuid, messageText);
        
        // Broadcast das neue Ticket an alle verbundenen Clients
        messagingTemplate.convertAndSend("/topic/ticket-updates", ticket);
        
        log.info("Successfully created ticket via WebSocket: {}", ticket.getId());
        return ticket;
    }

    /**
     * Aktualisiert den Status eines bestehenden Tickets über WebSocket.
     * Sendet Statusänderungen an alle interessierten Clients.
     *
     * @param ticketId Identifier des zu aktualisierenden Tickets
     * @param newStatus Neuer Status (z.B. "IN_PROGRESS", "RESOLVED")
     * @return Aktualisiertes Support-Ticket
     */
    @MessageMapping("/tickets/update-status")
    public SupportTicket updateTicketStatus(@Payload UUID ticketId, @Payload String newStatus) {
        log.info("Updating ticket status via WebSocket: {} -> {}", ticketId, newStatus);
        
        SupportTicket updatedTicket = ticketService.updateTicketStatus(ticketId, newStatus);
        
        // Broadcast Statusänderung an alle verbundenen Agenten/Clients
        messagingTemplate.convertAndSend("/topic/ticket-status-updates", updatedTicket);
        
        log.info("Ticket status updated successfully: {}", ticketId);
        return updatedTicket;
    }

    /**
     * Sendet eine Agent-Antwort an den spezifischen Kunden über User-Queue.
     * Nutzt STOMP's User-Destination-Feature für 1:1-Kommunikation.
     *
     * @param ticketId Identifier des betreffenden Tickets
     * @param agentResponse Text der Agenten-Antwort
     * @param customerId Identifier des Kunden
     */
    @MessageMapping("/tickets/send-response")
    public void sendAgentResponse(@Payload UUID ticketId, @Payload String agentResponse,
                                  @Payload UUID customerId) {
        log.info("Sending agent response for ticket: {}, customer: {}", ticketId, customerId);
        
        SupportTicket ticket = ticketService.addResponseToTicket(ticketId, agentResponse);
        
        // Sende Response nur zum spezifischen Kunden via User-Queue
        messagingTemplate.convertAndSendToUser(
            customerId.toString(),
            "/queue/ticket-responses",
            ticket
        );
        
        log.info("Agent response sent successfully to user queue");
    }
}