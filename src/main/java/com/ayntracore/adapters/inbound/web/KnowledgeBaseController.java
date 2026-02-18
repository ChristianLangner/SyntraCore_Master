package com.ayntracore.adapters.inbound.web;

import com.ayntracore.core.domain.KnowledgeEntry;
import com.ayntracore.core.ports.KnowledgeBasePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Inbound adapter for handling HTTP requests related to the knowledge base.
 * Implements the API endpoints for fetching knowledge entries.
 *
 * @implements KnowledgeBasePort (indirectly via the TicketUseCase)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class KnowledgeBaseController {

    private final KnowledgeBasePort knowledgeBase;

    /**
     * Fetches a single knowledge document by its unique ID.
     * This endpoint ensures that the provided ID is a valid UUID before processing.
     *
     * @param companyId The ID of the company (tenant) for data isolation.
     * @param id        The string representation of the document's UUID.
     * @return A ResponseEntity containing the KnowledgeEntry or a 404/400 error.
     */
    @GetMapping("/doc_fetch")
    public ResponseEntity<?> getDocumentById(
            @RequestParam("companyId") UUID companyId,
            @RequestParam("id") String id) {

        try {
            // --- Type Safety ---
            // Convert the string ID from the request into a UUID object.
            final UUID docId = UUID.fromString(id);

            // The KnowledgeBasePort is expected to handle the multi-tenancy check.
            return knowledgeBase.findById(docId, companyId)
                    .map(ResponseEntity::ok) // Found: Return 200 OK with the entry
                    .orElse(ResponseEntity.notFound().build()); // Not Found: Return 404

        } catch (IllegalArgumentException e) {
            // --- Error Handling ---
            // If UUID.fromString fails, it means the client sent an invalid ID format.
            log.error("Invalid UUID format for id: {}. CompanyId: {}", id, companyId, e);
            return ResponseEntity.badRequest().body("Error: Invalid ID format. Please provide a valid UUID.");
        }
    }
}
