// JPA-Entity um companyId erweitert, damit Hibernate die Spalte in der DB anlegt.
package com.syntracore.adapters.outbound.database;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "knowledge_base")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class KnowledgeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String category;

    private String source;

    /** * NEU: Die Datenbank-Spalte für den Mandanten.
     * Sorgt dafür, dass wir in SQL nach der Firma filtern können.
     */
    private UUID companyId;
}