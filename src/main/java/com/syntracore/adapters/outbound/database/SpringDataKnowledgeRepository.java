// UPDATE #11: Repository für die Wissensdatenbank
// Zweck: Ermöglicht CRUD-Operationen auf der knowledge_base Tabelle
// Ort: src/main/java/com/syntracore/adapters/outbound/database/SpringDataKnowledgeRepository.java

package com.syntracore.adapters.outbound.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Spring Data JPA Repository für Wissenseinträge.
 * Erledigt die ganze SQL-Arbeit automatisch für uns.
 */
@Repository
public interface SpringDataKnowledgeRepository extends JpaRepository<KnowledgeJpaEntity, UUID> {
    // Hier können wir später spezialisierte Suchen hinzufügen
}