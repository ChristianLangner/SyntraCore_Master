package com.ayntracore.adapters.outbound.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Spring Data JPA Repository für Wissenseinträge.
 * <p>
 * Implementiert automatische CRUD-Operationen auf der knowledge_base Tabelle.
 * Vereinfacht komplexe SQL-Abfragen durch method naming conventions.
 * </p>
 * 
 * <p><strong>Architektur-Schicht:</strong> Outbound-Adapterschicht (Data Access Layer)</p>
 * <p><strong>Zweck:</strong> Automatische Bereitstellung von Datenzugriffsmethoden mittels Spring Data JPA</p>
 * 
 * <h2>Spring Data JPA Prinzipien:</h2>
 * <ul>
 *   <li><strong>Repository Pattern:</strong> Abstraktion des Datenzugriffs vom Domain-Modell</li>
 *   <li><strong>Method Naming:</strong> Automatische Query-Generierung aus Methodennamen</li>
 *   <li><strong>Default Methods:</strong> CRUD-Operationen automatisch implementiert</li>
 *   <li><strong>Type Safety:</strong> Generische Typen für compile-time safety</li>
 * </ul>
 * 
 * <h2>Erweiterbarkeit:</h2>
 * <p>Spezialisierte Suchmethoden können per method naming hinzugefügt werden:</p>
 * <ul>
 *   <li>findByCategory(Category category)</li>
 *   <li>findByContentContaining(String keyword)</li>
 *   <li>findBySource(String source)</li>
 * </ul>
 * 
 * @author Christian Langner
 * @version 2.0
 * @since 2026
 * 
 * @see com.ayntracore.adapters.outbound.database.KnowledgeJpaEntity
 * @see org.springframework.data.repository.CrudRepository
 */
@Repository
public interface SpringDataKnowledgeRepository extends JpaRepository<KnowledgeJpaEntity, UUID> {
    /**
     * Erweiterungsmöglichkeiten für spezielle Wissensbasis-Suchen.
     * Beispiele für Method-Naming Conventions:
     * 
     * List<KnowledgeJpaEntity> findByCategory(String category);
     * List<KnowledgeJpaEntity> findByContentContaining(String keyword);
     * List<KnowledgeJpaEntity> findBySource(String source);
     */
}