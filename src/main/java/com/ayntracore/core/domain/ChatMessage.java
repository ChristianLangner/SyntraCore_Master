package com.ayntracore.core.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChatMessage(
    UUID id,
    UUID companyId,
    String role,
    String content,
    OffsetDateTime timestamp
) {}
