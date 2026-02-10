package com.ayntracore.adapters.inbound.web;

import com.ayntracore.core.domain.KnowledgeEntry;
import com.ayntracore.core.ports.KnowledgeBasePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class KnowledgeBaseController {

    private final KnowledgeBasePort knowledgeBase;

    @GetMapping("/doc_fetch")
    public ResponseEntity<KnowledgeEntry> getDocumentById(
            @RequestParam("companyId") UUID companyId,
            @RequestParam("id") String id) {

        // The KnowledgeBasePort is expected to handle the multi-tenancy check
        return knowledgeBase.findById(id, companyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
