
package com.ayntracore.adapters.outbound.database;

import com.ayntracore.adapters.outbound.database.ChatMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataChatMessageRepository extends JpaRepository<ChatMessageJpaEntity, UUID> {
    List<ChatMessageJpaEntity> findTop10ByCompanyIdOrderByTimestampDesc(UUID companyId);
}
