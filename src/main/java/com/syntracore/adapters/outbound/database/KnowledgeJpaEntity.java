// UPDATE #9: JPA-Entität für Wissenseinträge
// Zweck: Speicherung von Handbuch-Fragmenten in der H2/Supabase Datenbank
// Ort: src/main/java/com/syntracore/adapters/outbound/database/KnowledgeJpaEntity.java

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

    @Column(columnDefinition = "TEXT") // Damit auch lange Handbuchseiten reinpassen
    private String content;

    private String category; // z.B. "Installation", "Login"

    private String source; // Name der Datei oder des Wiki-Links
}