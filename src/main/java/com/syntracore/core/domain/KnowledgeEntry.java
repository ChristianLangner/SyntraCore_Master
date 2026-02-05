// UPDATE #29: Flexibler Konstruktor für KnowledgeEntry
// Ort: src/main/java/com/syntracore/core/domain/KnowledgeEntry.java

package com.syntracore.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeEntry {
    private UUID id;
    private String content;
    private String source;
    private String category;

    // NEU: Bequemer Konstruktor für den AdminController
    public KnowledgeEntry(String category, String content) {
        this.id = UUID.randomUUID(); // Generiert die ID automatisch
        this.category = category;
        this.content = content;
        this.source = "Admin Ingest";
    }
}