
package com.ayntracore.adapters.outbound.database;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_messages")
public class ChatMessageJpaEntity {

    @Id
    private UUID id;
    private UUID companyId;
    private String role;
    private String content;
    private OffsetDateTime timestamp;
}
