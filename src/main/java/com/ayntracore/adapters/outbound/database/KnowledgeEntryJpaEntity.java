
package com.ayntracore.adapters.outbound.database;

import com.pgvector.PGvector;
import io.hypersistence.utils.hibernate.type.vector.PGvectorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "knowledge_entry")
@Getter
@Setter
public class KnowledgeEntryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Type(PGvectorType.class)
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private PGvector embedding;

    @Column(name = "source_id")
    private String sourceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
