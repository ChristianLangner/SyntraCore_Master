package com.ayntracore.adapters.inbound.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AgentResponse {
    private String shortAnswer;
    private String longAnswer;
    private List<Source> sources;
    private UiHints uiHints;
}
