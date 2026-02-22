
package com.ayntracore.adapters.outbound.persistence;

import com.ayntracore.adapters.outbound.database.ChatMessageJpaEntity;
import com.ayntracore.adapters.outbound.database.SpringDataChatMessageRepository;
import com.ayntracore.core.domain.ChatMessage;
import com.ayntracore.core.ports.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ChatMessagePostgresRepository implements ChatMessageRepository {

    private final SpringDataChatMessageRepository chatMessageRepository;

    @Override
    public List<ChatMessage> findTop10ByCompanyIdOrderByTimestampDesc(UUID companyId) {
        return chatMessageRepository.findTop10ByCompanyIdOrderByTimestampDesc(companyId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private ChatMessage toDomain(ChatMessageJpaEntity entity) {
        return new ChatMessage(
                entity.getId(),
                entity.getCompanyId(),
                entity.getRole(),
                entity.getContent(),
                entity.getTimestamp()
        );
    }
}
