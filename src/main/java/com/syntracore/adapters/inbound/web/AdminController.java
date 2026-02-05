// UPDATE #17: Admin-Controller zum Verwalten des Wissens
// Ort: src/main/java/com/syntracore/web/AdminController.java

package com.syntracore.web;

import com.syntracore.core.domain.KnowledgeEntry;
import com.syntracore.core.domain.Ticket;
import com.syntracore.core.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TicketService ticketService;

    @GetMapping
    public String adminPage(Model model) {
        // Fügt das aktuelle Wissen und offene Tickets zum Model hinzu
        // Damit können wir es in der HTML-Seite anzeigen
        model.addAttribute("knowledgeEntries", ticketService.getAllKnowledge());
        model.addAttribute("openTickets", ticketService.getOpenTickets());
        return "admin"; // Verweist auf admin.html
    }

    @PostMapping("/add-knowledge")
    public String addKnowledge(@RequestParam String category, @RequestParam String content) {
        ticketService.addKnowledge(new KnowledgeEntry(category, content));
        return "redirect:/admin"; // Nach dem Speichern zurück zur Admin-Seite
    }

    @PostMapping("/resolve-ticket")
    public String resolveTicket(@RequestParam Long ticketId) {
        ticketService.resolveTicket(ticketId);
        return "redirect:/admin";
    }

    @PostMapping("/delete-knowledge")
    public String deleteKnowledge(@RequestParam Long knowledgeId) {
        // Hier müsste im TicketService eine Methode zum Löschen implementiert werden
        // Für den Anfang lassen wir es weg, um es einfach zu halten
        return "redirect:/admin";
    }
}