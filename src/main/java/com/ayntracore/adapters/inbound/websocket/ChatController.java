// Autor: Christian Langner
package com.ayntracore.adapters.inbound.websocket;

import com.ayntracore.core.ports.TicketUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import java.util.UUID;

/**
 * Inbound-Adapter – WebSocket-Controller für Echtzeit-KI-Chat.
 * <p>
 * Spezialisierter Adapter für WebSocket-Kommunikation im Ticket-Kontext.
 * Ermöglicht Live-Chat-Funktionalität mit sofortiger KI-Antwort über
 * STOMP-Protokoll. Implementiert Reactive Messaging Pattern.
 * </p>
 *
 * @see Inbound-Adapter gemäß hexagonaler Architektur
 * @see Echtzeit-Adapter für Pub/Sub-Kommunikation
 * @author Christian Langner
 * @version 2.0
 * @since 2026
 */
@Controller
@RequiredArgsConstructor
public class ChatController {

    /**
     * Inbound Port für die Anwendungslogik-Integration.
     * Gewährleistet lose Kopplung zwischen Adapter und Core-Logik.
     */
    private final TicketUseCase ticketUseCase;

    @Value("${ayntra.persona.company-id:default-company}")
    private String companyId;

    /**
     * Verarbeitet eingehende WebSocket-Nachrichten über STOMP-Protokoll.
     * Implementiert den Live-Chat-Workflow mit vollständigem RAG-Prozess.
     *
     * @param message Benutzeranfrage im Chat-Format
     * @return KI-generierte Antwort, die an alle verbundenen Clients broadcastet wird
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public String handleChatMessage(String message) {
        return ticketUseCase.processInquiry(message, UUID.fromString(companyId));
    }
}
