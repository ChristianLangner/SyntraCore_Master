// UPDATE #2: WebSocket-Chat-Adapter
// Zweck: Empfängt Live-Chat-Anfragen und delegiert an den TicketUseCase
// Ort: src/main/java/com/syntracore/adapters/inbound/websocket/ChatController.java

package com.syntracore.adapters.inbound.websocket;

import com.syntracore.core.ports.TicketUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * Inbound-Adapter für die WebSocket-Kommunikation.
 * Verarbeitet eingehende Nachrichten im Live-Chat.
 */
@Controller
@RequiredArgsConstructor
public class ChatController {

    // Wir nutzen den Inbound Port (Interface) für die hexagonale Trennung
    private final TicketUseCase ticketUseCase;

    /**
     * Wird aufgerufen, wenn ein User eine Nachricht an /app/chat.sendMessage schickt.
     * @param message Die Textnachricht des Users
     * @return Die Antwort der KI, die automatisch an /topic/public verteilt wird
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public String handleChatMessage(String message) {
        System.out.println("💬 WebSocket-Nachricht empfangen: " + message);

        // Delegierung an den Core-Service über den Port
        return ticketUseCase.processInquiry(message);
    }
}