// Autor: Christian Langner
package com.ayntracore.adapters.outbound.database;

import com.ayntracore.core.domain.KnowledgeEntry;
import com.ayntracore.core.ports.KnowledgeBasePort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Outbound-Adapter – Persistiert Wissenseinträge in relationaler Datenbank.
 * <p>
 * Implementiert den KnowledgeBasePort für JPA-basierte Datenzugriffe.
 * Übersetzt zwischen Domain-Modellen und JPA-Entities gemäß hexagonaler Architektur.
 * Aktiv nur im 'school'-Profil für lokale Schul-Umgebungen.
 * </p>
 *
 * <p><strong>Architektur-Schicht:</strong> Outbound-Adapter (Profile: school)</p>
 * <p><strong>Zweck:</strong> JPA-basierte Persistierung für Wissensbasisdaten</p>
 *
 * <h2>Profil-Basierte Aktivierung:</h2>
 * <ul>
 *   <li><strong>School-Profil:</strong> Lokale relationale Datenbank für Schul-Umgebungen</li>
 *   <li><strong>Resilienz-Strategie:</strong> Profile 'school' vs. 'home' für unterschiedliche Deployment-Szenarien</li>
 * </ul>
 *
 * @author Christian Langner
 * @version 2.0
 * @see com.ayntracore.core.domain.KnowledgeEntry
 * @see com.ayntracore.adapters.outbound.database.VectorKnowledgeAdapter
 * @since 2026
 */
@Component
// Wir lassen den Adapter für beide Profile aktiv (school für H2, home für Supabase)
@Profile({"school", "home"})
@RequiredArgsConstructor
public class DatabaseKnowledgeAdapter implements KnowledgeBasePort {

    private final SpringDataKnowledgeRepository repository;

    /**
     * Filtert Wissen nun strikt nach der übergebenen companyId.
     */
    @Override
    public List<String> findRelevantContext(String query, UUID companyId) {
        return repository.findAll().stream()
                // ÄNDERUNG: Strikte Filterung nach Mandant
                .filter(entity -> entity.getCompanyId() != null && entity.getCompanyId().equals(companyId))
                .filter(entity -> entity.getContent().toLowerCase().contains(query.toLowerCase())
                        || entity.getCategory().toLowerCase().contains(query.toLowerCase()))
                .map(KnowledgeJpaEntity::getContent)
                .collect(Collectors.toList());
    }

    @Override
    public KnowledgeEntry save(KnowledgeEntry entry) {
        // Mapping inkl. companyId using Builder
        KnowledgeJpaEntity jpaEntity = KnowledgeJpaEntity.builder()
                .id(entry.getId())
                .content(entry.getContent())
                .category(entry.getCategory())
                .source(entry.getSource())
                .companyId(entry.getCompanyId())
                .build();
        KnowledgeJpaEntity savedEntity = repository.save(jpaEntity);
        return new KnowledgeEntry(
                savedEntity.getId(),
                savedEntity.getContent(),
                savedEntity.getSource(),
                savedEntity.getCategory(),
                savedEntity.getCompanyId()
        );
    }

    @Override
    public List<KnowledgeEntry> findAll() {
        return repository.findAll().stream()
                .map(entity -> new KnowledgeEntry(
                        entity.getId(),
                        entity.getContent(),
                        entity.getSource(),
                        entity.getCategory(),
                        entity.getCompanyId()
                ))
                .collect(Collectors.toList());
    }
}
