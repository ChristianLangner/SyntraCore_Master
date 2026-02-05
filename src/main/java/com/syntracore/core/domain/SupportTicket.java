// UPDATE #31: REVIDIERTES Domain-Modell (Inhalt korrigiert)
// Zweck: Reine Datenhaltung für Tickets ohne Service-Logik
// Ort: src/main/java/com/syntracore/core/domain/SupportTicket.java

package com.syntracore.core.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Das fachliche Herzstück eines Support-Tickets.
 * Hier fließen keine Spring-Abhängigkeiten ein.
 */
@Getter
@Setter
public class SupportTicket {

    private UUID id;
    private String customerName;
    private String message;
    private LocalDateTime createdAt;
    private String aiAnalysis;
    private boolean resolved; // Neu hinzugefügt für die Admin-Logik

    public SupportTicket(String customerName, String message) {
        this.id = UUID.randomUUID();
        this.customerName = customerName;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.resolved = false;
    }
}