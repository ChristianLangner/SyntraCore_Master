// UPDATE #3: Definition des Inbound Ports (Driving Port)
// Zweck: Abstraktion der Geschäftslogik für Inbound-Adapter (Web, WebSockets, Telegram)
// Ort: src/main/java/com/syntracore/core/ports/TicketUseCase.java

package com.syntracore.core.ports;

/**
 * Inbound Port für die Ticket-Verarbeitung.
 * * <p>Dieses Interface definiert die Use Cases, die von außen (Adaptern) 
 * aufgerufen werden können. Es entkoppelt die Adapter von der konkreten 
 * Service-Implementierung.</p>
 * * @author SyntraCore Development Team
 * @version 1.0
 */
public interface TicketUseCase {

    /**
     * Erstellt ein neues Support-Ticket und stößt die KI-Analyse an.
     * @param customerName Name des Absenders
     * @param message Inhalt der Anfrage
     */
    void createAndProcessTicket(String customerName, String message);

    /**
     * Verarbeitet eine Live-Anfrage direkt (für den Live-Chat).
     * @param userMessage Die Nachricht des Benutzers
     * @return Die Antwort der KI
     */
    String processInquiry(String userMessage);
}