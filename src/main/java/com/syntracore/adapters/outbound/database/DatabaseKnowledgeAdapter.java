package com.syntracore.adapters.outbound.database;

import com.syntracore.core.domain.KnowledgeEntry;
import com.syntracore.core.ports.KnowledgeBasePort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
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
 * @since 2026
 * 
 * @see com.syntracore.core.domain.KnowledgeEntry
 * @see com.syntracore.adapters.outbound.database.VectorKnowledgeAdapter
 */
@Component
@Profile("school")
@RequiredArgsConstructor
public class DatabaseKnowledgeAdapter implements KnowledgeBasePort {

    /**
     * Spring Data Repository für JPA-Operationen.
     * Gewährleistet lose Kopplung zwischen Adapter und JPA-Implementation.
     */
    private final SpringDataKnowledgeRepository repository;

    /**
     * Sucht Wissenseinträge nach Inhalt und Kategorie.
     * Implementiert einfache String-Matching-Logik für semantische Suche.
     *
     * @param query Suchbegriff für Inhalte und Kategorien
     * @return Liste von Treffern als Strings
     */
    @Override
    public List<String> findRelevantContext(String query) {
        List<String> results = repository.findAll().stream()
                .filter(entity -> entity.getContent().toLowerCase().contains(query.toLowerCase())
                        || entity.getCategory().toLowerCase().contains(query.toLowerCase()))
                .map(KnowledgeJpaEntity::getContent)
                .collect(Collectors.toList());
        System.out.println("🔍 Wissensbasis-Suche: " + query + " -> Ergebnisse: " + results.size());
        return results;
    }

    /**
     * Speichert einen neuen Wissenseintrag in der Datenbank.
     * Übersetzt Domain-Modell zu JPA-Entity und persistiert.
     *
     * @param entry Domain-Modell des Wissenseintrags mittels UUID
     * @return Persistierter Wissenseintrag als Domain-Modell
     */
    @Override
    public KnowledgeEntry save(KnowledgeEntry entry) {
        KnowledgeJpaEntity jpaEntity = new KnowledgeJpaEntity(entry.getId(), entry.getContent(), entry.getCategory(), entry.getSource());
        KnowledgeJpaEntity savedEntity = repository.save(jpaEntity);
        System.out.println("💾 Wissenseintrag gespeichert: " + savedEntity.getId());
        return new KnowledgeEntry(savedEntity.getId(), savedEntity.getContent(), savedEntity.getSource(), savedEntity.getCategory());
    }

    /**
     * Liest alle Wissenseinträge aus der Datenbank.
     * Übersetzt JPA-Entities zurück zu Domain-Modellen.
     *
     * @return Liste aller Wissenseinträge als Domain-Modelle
     */
    @Override
    public List<KnowledgeEntry> findAll() {
        return repository.findAll().stream()
                .map(entity -> new KnowledgeEntry(entity.getId(), entity.getContent(), entity.getSource(), entity.getCategory()))
                .collect(Collectors.toList());
    }
}