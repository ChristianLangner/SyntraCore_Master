// UPDATE #15: Intelligenter H2-Adapter für die Schule
// Ort: src/main/java/com/syntracore/adapters/outbound/database/DatabaseKnowledgeAdapter.java

package com.syntracore.adapters.outbound.database;

import com.syntracore.core.ports.KnowledgeBasePort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("school") // Aktiv, wenn spring.profiles.active=school
@RequiredArgsConstructor
public class DatabaseKnowledgeAdapter implements KnowledgeBasePort {

    private final SpringDataKnowledgeRepository repository;

    @Override
    public List<String> findRelevantContext(String query) {
        String lowerQuery = query.toLowerCase();

        // Simuliert RAG: Sucht nach Übereinstimmungen im Inhalt
        return repository.findAll().stream()
                .filter(entry -> isRelevant(lowerQuery, entry))
                .map(KnowledgeJpaEntity::getContent)
                .collect(Collectors.toList());
    }

    private boolean isRelevant(String query, KnowledgeJpaEntity entry) {
        // Findet Einträge, wenn Kategorie oder Inhalt Schlagwörter der Frage enthalten
        return entry.getCategory().toLowerCase().contains(query) ||
                entry.getContent().toLowerCase().contains(query) ||
                query.split(" ").length > 2; // Simpler Fallback für Demo
    }
}