package com.syntracore.adapters.inbound.websocket;

import com.syntracore.core.ports.TicketUseCase;
import lombok.RequiredArgsConstructor;
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

    // Wir definieren eine feste Test-ID, bis wir ein Login-System haben
    private static final UUID TEST_COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

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
        // ÄNDERUNG: TEST_COMPANY_ID wird jetzt mitgegeben
        return ticketUseCase.processInquiry(message, TEST_COMPANY_ID);
    }
}