// UPDATE #28: DatabaseKnowledgeAdapter mit allen Port-Methoden
// Ort: src/main/java/com/syntracore/adapters/outbound/database/DatabaseKnowledgeAdapter.java

package com.syntracore.adapters.outbound.database;

import com.syntracore.core.domain.KnowledgeEntry;
import com.syntracore.core.ports.KnowledgeBasePort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("school")
@RequiredArgsConstructor
public class DatabaseKnowledgeAdapter implements KnowledgeBasePort {

    private final SpringDataKnowledgeRepository repository;

    @Override
    public List<String> findRelevantContext(String query) {
        return repository.findAll().stream()
                .filter(e -> e.getContent().toLowerCase().contains(query.toLowerCase())
                        || e.getCategory().toLowerCase().contains(query.toLowerCase()))
                .map(KnowledgeJpaEntity::getContent)
                .collect(Collectors.toList());
    }

    @Override
    public KnowledgeEntry save(KnowledgeEntry entry) {
        KnowledgeJpaEntity jpa = new KnowledgeJpaEntity(entry.getId(), entry.getContent(), entry.getCategory(), entry.getSource());
        KnowledgeJpaEntity saved = repository.save(jpa);
        return new KnowledgeEntry(saved.getId(), saved.getContent(), saved.getSource(), saved.getCategory());
    }

    @Override
    public List<KnowledgeEntry> findAll() {
        return repository.findAll().stream()
                .map(e -> new KnowledgeEntry(e.getId(), e.getContent(), e.getSource(), e.getCategory()))
                .collect(Collectors.toList());
    }
}