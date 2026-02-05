package com.syntracore.adapters.inbound.web;

import com.syntracore.core.domain.KnowledgeEntry;
import com.syntracore.core.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

/**
 * Inbound-Adapter – Admin-Controller für administrative Funktionen.
 * <p>
 * Primärer Adapter für die Admin-Webschnittstelle. Stellt HTTP-Endpunkte
 * zur Verfügung für Knowledge-Management und Ticket-Administration.
 * Nutzt den konkreten TicketService direkt für vereinfachte Abhängigkeiten.
 * </p>
 * @see Inbound-Adapter gemäß hexagonaler Architektur
 * @see Primär-Adapter: Klassische Webanwendung mit Thymeleaf-UI
 * @author Christian Langner
 * @version 2.0
 * @since 2026
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    /**
     * TicketService für Zugriff auf alle administrativen Operationen.
     * Ermöglicht Knowledge-Einträge-Verwaltung und Ticket-Resolution.
     */
    private final TicketService ticketService;

    /**
     * Lädt die Admin-Dashboard-Seite mit aktuellen Daten.
     * Kombiniert Knowledge-Einträge und offene Tickets in einem View.
     *
     * @param model Spring Model zur Datenübertragung an die View
     * @return Name der Thymeleaf-Template-Datei (admin.html)
     */
    @GetMapping
    public String adminPage(Model model) {
        model.addAttribute("knowledgeEntries", ticketService.getAllKnowledge());
        model.addAttribute("openTickets", ticketService.getAllTickets());
        return "admin";
    }

    /**
     * Fügt einen neuen Knowledge-Eintrag zur Wissensbasis hinzu.
     * Unterstützt formularbasierte Dateneingabe.
     *
     * @param category Kategorie für die semantische Gruppierung
     * @param content Inhaltlicher Wissenseintrag
     * @return Redirect zur Admin-Seite zur Darstellung der Aktualisierung
     */
    @PostMapping("/add-knowledge")
    public String addKnowledge(@RequestParam String category, @RequestParam String content) {
        ticketService.addKnowledge(new KnowledgeEntry(category, content));
        return "redirect:/admin";
    }

    /**
     * Markiert ein Support-Ticket als gelöst.
     * Verwendet UUID-Primärschlüssel für referenzielle Integrität.
     *
     * @param ticketId UUID-String des zu lösenden Tickets
     * @return Redirect zur Admin-Seite zur Aktualisierung der Ticketliste
     */
    @PostMapping("/resolve-ticket")
    public String resolveTicket(@RequestParam String ticketId) {
        ticketService.resolveTicket(UUID.fromString(ticketId));
        return "redirect:/admin";
    }
}