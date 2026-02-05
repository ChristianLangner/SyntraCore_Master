// UPDATE #30: Korrigierter AdminController (UUID-kompatibel)
// Ort: src/main/java/com/syntracore/adapters/inbound/web/AdminController.java

package com.syntracore.web; // Achte darauf, dass das Package zu deiner Ordnerstruktur passt!

import com.syntracore.core.domain.KnowledgeEntry;
import com.syntracore.core.domain.SupportTicket;
import com.syntracore.core.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TicketService ticketService;

    @GetMapping
    public String adminPage(Model model) {
        model.addAttribute("knowledgeEntries", ticketService.getAllKnowledge());
        model.addAttribute("openTickets", ticketService.getAllTickets()); // Geändert von getOpenTickets
        return "admin";
    }

    @PostMapping("/add-knowledge")
    public String addKnowledge(@RequestParam String category, @RequestParam String content) {
        // Nutzt jetzt den neuen bequemen Konstruktor
        ticketService.addKnowledge(new KnowledgeEntry(category, content));
        return "redirect:/admin";
    }

    @PostMapping("/resolve-ticket")
    public String resolveTicket(@RequestParam String ticketId) {
        // Konvertiert den String aus dem Formular in eine UUID
        ticketService.resolveTicket(UUID.fromString(ticketId));
        return "redirect:/admin";
    }
}