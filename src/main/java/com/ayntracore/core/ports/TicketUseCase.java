package com.ayntracore.core.ports;

import java.util.UUID;

/**
 * Inbound Port – Driving Port für die Ticket-Verarbeitung.
 * <p>
 * Dieser Port definiert die Geschäftsanforderungen, die von außen
 * (Inbound-Adaptern wie Web-Controller, WebSocket-Endpunkte) aufgerufen
 * werden können. Er isoliert die Anwendungslogik von konkreten Framework- 
 * und Infrastrukturdetails.
 * </p>
 * 
 * @see Inbound-Port gemäß hexagonaler Architektur
 * @author Christian Langner
 * @version 2.0
 * @since 2026
 */
public interface TicketUseCase {

    /**
     * Erstellt ein neues Support-Ticket und initiiert die KI-gestützte Analyse.
     * Diese Methode folgt dem RAG-Workflow (Retrieval-Augmented Generation).
     *
     * @param customerName Name des Absenders (Kunde oder Benutzer)
     * @param message Inhalt der Support-Anfrage
     */
    void createAndProcessTicket(String customerName, String message, UUID customerId);

    /**
     * Verarbeitet eine Live-Anfrage direkt ohne Ticket-Erstellung.
     * Wird für den interaktiven Live-Chat verwendet.
     * <p>
     * Der Unterschied zur Ticket-Verarbeitung liegt in der
     * Priorität und Antwortgeschwindigkeit.
     * </p>
     *
     * @param userMessage Die Nachricht des Benutzers
     * @return KI-basierte Antwort in Echtzeit
     */
    String processInquiry(String userMessage, UUID customerId);
}