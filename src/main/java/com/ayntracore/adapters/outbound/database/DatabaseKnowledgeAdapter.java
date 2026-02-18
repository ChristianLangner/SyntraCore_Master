// Autor: Christian Langner
package com.ayntracore.adapters.outbound.database;

import com.ayntracore.core.domain.KnowledgeEntry;
import com.ayntracore.core.ports.KnowledgeBasePort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Profile({"school", "home"})
@RequiredArgsConstructor
public class DatabaseKnowledgeAdapter implements KnowledgeBasePort {

    private final SpringDataKnowledgeRepository repository;

    @Override
    public List<String> findRelevantContext(String query, UUID companyId) {
        return repository.findAll().stream()
                .filter(entity -> entity.getCompanyId() != null && entity.getCompanyId().equals(companyId))
                .filter(entity -> entity.getContent().toLowerCase().contains(query.toLowerCase())
                        || entity.getCategory().toLowerCase().contains(query.toLowerCase()))
                .map(KnowledgeJpaEntity::getContent)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<KnowledgeEntry> findById(UUID id, UUID companyId) {
        return repository.findById(id)
                .filter(entity -> entity.getCompanyId().equals(companyId))
                .map(this::mapToDomain);
    }

    @Override
    public List<KnowledgeEntry> findRelevantEntries(String query, UUID companyId, String category) {
        return repository.findAll().stream()
                .filter(entity -> entity.getCompanyId().equals(companyId))
                .filter(entity -> category == null || entity.getCategory().equalsIgnoreCase(category))
                .filter(entity -> entity.getContent().toLowerCase().contains(query.toLowerCase()))
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public KnowledgeEntry save(KnowledgeEntry entry) {
        KnowledgeJpaEntity jpaEntity = mapToJpa(entry);
        KnowledgeJpaEntity savedEntity = repository.save(jpaEntity);
        return mapToDomain(savedEntity);
    }

    @Override
    public List<KnowledgeEntry> findAll() {
        return repository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    /**
     * Maps a KnowledgeJpaEntity to a KnowledgeEntry domain object using the builder.
     * This centralization avoids repetition and simplifies constructor changes.
     */
    private KnowledgeEntry mapToDomain(KnowledgeJpaEntity entity) {
        return KnowledgeEntry.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .category(entity.getCategory())
                .content(entity.getContent())
                .source(entity.getSource())
                // Embeddings are handled by the VectorSearchAdapter, so this can be null here.
                .build();
    }

    /**
     * Maps a KnowledgeEntry domain object to a KnowledgeJpaEntity.
     */
    private KnowledgeJpaEntity mapToJpa(KnowledgeEntry entry) {
        return KnowledgeJpaEntity.builder()
                .id(entry.getId())
                .companyId(entry.getCompanyId())
                .category(entry.getCategory())
                .content(entry.getContent())
                .source(entry.getSource())
                .build();
    }
}
