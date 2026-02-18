package com.ayntracore.adapters.inbound.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Source {
    private String sourceName;
    private double relevance;
    private String link;
}
