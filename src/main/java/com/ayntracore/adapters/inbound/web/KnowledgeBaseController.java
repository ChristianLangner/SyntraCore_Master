package com.ayntracore.adapters.inbound.web;

import com.ayntracore.core.domain.KnowledgeEntry;
import com.ayntracore.core.ports.KnowledgeBasePort;
import com.ayntracore.core.ports.VectorSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Inbound adapter for handling HTTP requests related to the knowledge base.
 * Implements the API endpoints for fetching and uploading knowledge entries.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/knowledge")
@Slf4j
public class KnowledgeBaseController {

    private final KnowledgeBasePort knowledgeBase;
    private final VectorSearchPort vectorSearchPort;

    /**
     * Fetches a single knowledge document by its unique ID.
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
            final UUID docId = UUID.fromString(id);
            return knowledgeBase.findById(docId, companyId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for id: {}. CompanyId: {}", id, companyId, e);
            return ResponseEntity.badRequest().body("Error: Invalid ID format. Please provide a valid UUID.");
        }
    }

    /**
     * Uploads a new knowledge document.
     *
     * @param file      The file to upload.
     * @param companyId The ID of the company (tenant).
     * @param source    The source of the document.
     * @return A ResponseEntity indicating the result of the operation.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadKnowledge(
            @RequestParam("file") MultipartFile file,
            @RequestParam("companyId") UUID companyId,
            @RequestParam("source") String source) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            String content = new String(file.getBytes());
            KnowledgeEntry entry = KnowledgeEntry.builder()
                    .id(UUID.randomUUID())
                    .companyId(companyId)
                    .source(source)
                    .content(content)
                    .build();

            vectorSearchPort.saveWithEmbedding(entry);
            log.info("Successfully uploaded and processed file: {}", file.getOriginalFilename());
            return ResponseEntity.ok("Dokument " + file.getOriginalFilename() + " erfolgreich gelernt!");
        } catch (IOException e) {
            log.error("Failed to read file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(500).body("Failed to read file");
        }
    }
}
