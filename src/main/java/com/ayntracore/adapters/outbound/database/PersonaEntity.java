package com.ayntracore.adapters.outbound.database;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "personas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PersonaEntity {
    @Id
    private String id;
    @Column(nullable = false)
    private String name;
    private String role;
    @Column(name = "persona_type")
    private String type;
    private boolean active;
}