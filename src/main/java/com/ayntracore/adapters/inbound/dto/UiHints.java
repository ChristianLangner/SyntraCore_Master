package com.ayntracore.adapters.inbound.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UiHints {
    private String primaryColor;
    private String theme;
    private String personaName;
}
