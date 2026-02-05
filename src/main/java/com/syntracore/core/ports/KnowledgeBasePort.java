// UPDATE #25: Bereinigter KnowledgeBasePort
// Ort: src/main/java/com/syntracore/core/ports/KnowledgeBasePort.java

package com.syntracore.core.ports;

import com.syntracore.core.domain.KnowledgeEntry;
import java.util.List;

public interface KnowledgeBasePort {
    List<String> findRelevantContext(String query);
    KnowledgeEntry save(KnowledgeEntry entry);
    List<KnowledgeEntry> findAll();
}