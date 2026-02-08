// Autor: Christian Langner
// JPA-Entity um companyId und embedding erweitert für RAG-Vektor-Integration.
package com.syntracore.adapters.outbound.database;

import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA-Entity für Knowledge-Tabelle mit pgvector-Integration.
 * Hexagonale Architektur: Infrastructure-Layer (Adapter).
 */
@Entity
@Table(name = "knowledge_base")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(length = 255)
    private String category;

    @Column(length = 500)
    private String source;

    /**
     * Vektor-Embedding (1536 Dimensionen für OpenAI text-embedding-3-small).
     * Verwendet pgvector für Ähnlichkeitssuche.
     */
    @Column(columnDefinition = "vector(1536)")
    private PGvector embedding;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}