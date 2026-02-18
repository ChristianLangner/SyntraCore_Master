package com.ayntracore.adapters.inbound.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentResponse {
    private String shortAnswer;
    private String longAnswer;
    private String imageUrl; // Field added to solve build issue
    private List<Source> sources;
    private UiHints uiHints;
}
