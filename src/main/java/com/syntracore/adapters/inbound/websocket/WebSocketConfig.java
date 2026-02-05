// UPDATE #1: WebSocket-Konfigurations-Adapter
// Zweck: Registrierung des STOMP-Endpunkts für die Echtzeit-Kommunikation
// Ort: src/main/java/com/syntracore/adapters/inbound/websocket/WebSocketConfig.java

package com.syntracore.adapters.inbound.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Konfiguration für die WebSocket-Infrastruktur.
 * Ermöglicht Echtzeit-Messaging über das STOMP-Protokoll.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Nachrichten vom Server an den Client (z.B. KI-Antworten)
        config.enableSimpleBroker("/topic");

        // Präfix für Nachrichten vom Client an den Server
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Der Endpunkt, an dem sich das Frontend anmeldet
        registry.addEndpoint("/ws-support").withSockJS();
    }
}