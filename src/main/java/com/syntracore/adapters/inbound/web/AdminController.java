package com.syntracore.adapters.inbound.web;

import com.syntracore.core.domain.KnowledgeEntry;
import com.syntracore.core.domain.Persona;
import com.syntracore.core.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * // UPDATE #58
 * Inbound-Adapter – Admin-Controller für administrative Funktionen.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TicketService ticketService;

    private static final UUID TEST_COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @GetMapping
    public String adminPage(Model model) {
        model.addAttribute("knowledgeEntries", ticketService.getAllKnowledge());
        model.addAttribute("openTickets", ticketService.getAllTickets());
        return "admin";
    }

    @GetMapping("/persona")
    @ResponseBody
    public Persona currentPersona() {
        return ticketService.getCurrentPersona(TEST_COMPANY_ID);
    }

    @PostMapping("/add-knowledge")
    public String addKnowledge(@RequestParam String category, @RequestParam String content) {
        ticketService.addKnowledge(new KnowledgeEntry(category, content, TEST_COMPANY_ID));
        return "redirect:/admin";
    }

    @PostMapping("/update-persona")
    public String updatePersona(@RequestParam("name") String name,
                                @RequestParam("prompt") String prompt,
                                @RequestParam("style") String style,
                                @RequestParam(value = "traitsJson", required = false) String traitsJson,
                                @RequestParam(value = "promptTemplate", required = false) String promptTemplate,
                                @RequestParam(value = "exampleDialog", required = false) String exampleDialog) {

        ticketService.updatePersona(TEST_COMPANY_ID, name, prompt, style, traitsJson, promptTemplate, exampleDialog);
        return "redirect:/admin";
    }

    @PostMapping("/resolve-ticket")
    public String resolveTicket(@RequestParam String ticketId) {
        ticketService.resolveTicket(UUID.fromString(ticketId));
        return "redirect:/admin";
    }
}