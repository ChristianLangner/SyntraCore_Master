// Autor: Christian Langner
package com.ayntracore.adapters.outbound.database;

import com.ayntracore.core.domain.PersonaType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JPA-Entity für Persona-Tabelle.
 * Hexagonale Architektur: Infrastructure-Layer (Adapter).
 * Cloud-spezifische Logik mit @Profile("home") guard.
 */
@Entity
@Table(name = "persona")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "persona_type", nullable = false)
    private PersonaType personaType;

    @Builder.Default
    @Column(name = "allow_explicit_content", nullable = false)
    private Boolean allowExplicitContent = false;

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(name = "speaking_style", length = 500)
    private String speakingStyle;

    /**
     * Frei erweiterbare Eigenschaften als JSONB.
     * Verwendet Hypersistence Utils für JSON-Mapping.
     */
    @Builder.Default
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> traits = new LinkedHashMap<>();

    @Column(name = "prompt_template", columnDefinition = "TEXT")
    private String promptTemplate;

    @Column(name = "example_dialog", columnDefinition = "TEXT")
    private String exampleDialog;

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
