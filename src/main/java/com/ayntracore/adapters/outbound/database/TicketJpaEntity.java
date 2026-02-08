// UPDATE #35: TicketJpaEntity (UUID-Key)
// Ort: src/main/java/com/ayntracore/adapters/outbound/database/TicketJpaEntity.java

package com.ayntracore.adapters.outbound.database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
public class TicketJpaEntity {

    @Id
    private UUID id;

    private String customerName;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String aiAnalysis;

    // Optional: Falls du den Status "gelöst" in der DB speichern willst
    private boolean resolved;

    // NEU: Hier fehlte die Variable, weshalb setCompanyId/getCompanyId nicht funktionierten
    private UUID companyId;
}