// UPDATE #8: Domain-Modell für Wissenseinträge
// Zweck: Speicherung von Handbuch-Fragmenten in der Datenbank
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
    private String content; // Der eigentliche Handbuch-Text
    private String source;  // z.B. "Benutzerhandbuch_V1.pdf"
    private String category; // z.B. "Installation"
}