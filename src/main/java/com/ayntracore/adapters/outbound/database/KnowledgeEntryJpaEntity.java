package com.ayntracore.adapters.outbound.database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private float[] embedding;

    @Column(name = "source_id")
    private String sourceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
