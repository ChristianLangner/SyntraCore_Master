// UPDATE #36: AdminController Finaler Fix
// Ort: src/main/java/com/syntracore/adapters/inbound/web/AdminController.java

package com.syntracore.adapters.inbound.web;

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
        // Nutzt getAllTickets() aus dem Service
        model.addAttribute("openTickets", ticketService.getAllTickets());
        return "admin";
    }

    @PostMapping("/add-knowledge")
    public String addKnowledge(@RequestParam String category, @RequestParam String content) {
        ticketService.addKnowledge(new KnowledgeEntry(category, content));
        return "redirect:/admin";
    }

    @PostMapping("/resolve-ticket")
    public String resolveTicket(@RequestParam String ticketId) {
        ticketService.resolveTicket(UUID.fromString(ticketId));
        return "redirect:/admin";
    }
}