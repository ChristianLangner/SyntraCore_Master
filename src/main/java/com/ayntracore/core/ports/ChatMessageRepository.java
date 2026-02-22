package com.ayntracore.core.ports;

import com.ayntracore.core.domain.ChatMessage;
import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository {
    List<ChatMessage> findTop10ByCompanyIdOrderByTimestampDesc(UUID companyId);
}
